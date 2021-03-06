/*
 * Accio is a program whose purpose is to study location privacy.
 * Copyright (C) 2016 Vincent Primault <vincent.primault@liris.cnrs.fr>
 *
 * Accio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Accio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Accio.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnrs.liris.accio.core.runtime

import java.nio.file.Path

import com.google.inject.{Inject, Injector}
import com.typesafe.scalalogging.StrictLogging
import fr.cnrs.liris.accio.core.api.{OpContext, Operator}
import fr.cnrs.liris.accio.core.framework._
import fr.cnrs.liris.common.util.Requirements._

import scala.collection.mutable
import scala.util.control.NonFatal

/**
 * This is the class responsible for managing the execution of an experiment. It handles provisoning, scheduling and
 * monitoring of experiments.
 *
 * @param repository Report repository.
 * @param runFactory Run factory.
 * @param opRegistry Operator registry.
 * @param injector   Guice injector.
 */
class ExperimentExecutor @Inject()(repository: ReportRepository, runFactory: RunFactory, opRegistry: OpRegistry, injector: Injector) extends StrictLogging {

  def execute(experiment: Experiment, workDir: Path, progressReporter: ProgressReporter): ExperimentReport = {
    var report = new ExperimentReport
    repository.write(workDir, experiment.copy(report = Some(report)))
    progressReporter.onStart(experiment)

    val runs = runFactory.create(experiment)
    runs.foreach { run =>
      logger.trace(s"Starting execution of workflow run ${run.id}")
      report = report.addRun(run.id)
      repository.write(workDir, experiment.copy(report = Some(report)))
      execute(run, experiment.workflow, workDir, progressReporter)
      logger.trace(s"Completed execution of workflow run ${run.id}")
    }

    report = report.complete()
    progressReporter.onComplete(experiment)
    repository.write(workDir, experiment.copy(report = Some(report)))
    logger.trace(s"Completed execution of experiment ${experiment.id}")

    report
  }

  /**
   * Execute a given run. Artifacts and reports will be written inside a working directory, and a progress reporter
   * will receive updates about the graph execution progress.
   *
   * @param run              Run to execute.
   * @param workflow         Workflow to execute.
   * @param workDir          Working directory where to write artifacts and reports.
   * @param progressReporter Reporter receiving progress updates.
   * @return Final execution report.
   */
  private def execute(run: Run, workflow: Workflow, workDir: Path, progressReporter: ProgressReporter = NoProgressReporter): RunReport = {
    val outputs = mutable.Map.empty[Reference, Artifact]
    val scheduled = mutable.Queue.empty[Node] ++ workflow.graph.roots // Initially schedule graph roots
    var lastError: Option[Throwable] = None
    var report = new RunReport

    writeReport(workDir, run, report, progressReporter.onGraphStart(run))

    while (scheduled.nonEmpty) {
      val node = scheduled.dequeue()
      report = report.startNode(node.name)

      // We write the report and report status before executing the node.
      writeReport(workDir, run, report, progressReporter.onNodeStart(run, node))

      try {
        val artifacts = execute(run.id, run.seed, run.params, node, outputs.toMap, workDir)
        report = report.completeNode(node.name, artifacts.values.toSeq)
        outputs ++= artifacts
      } catch {
        case NonFatal(e) =>
          report = report.completeNode(node.name, e)
          lastError = Some(e)
      }

      // We write the report and report status after executing the node.
      writeReport(workDir, run, report, progressReporter.onNodeComplete(run, node))

      // Then we can schedule next steps to take.
      if (lastError.isEmpty) {
        // If the node was successfully executed, we schedule its successors for which all dependencies are available.
        scheduled ++= node.successors.map(workflow.graph(_)).filter(_.dependencies.forall(outputs.contains))
      } else {
        // Otherwise we clear the remaining scheduled nodes to exit the loop.
        scheduled.clear()
      }
    }
    report = report.complete()

    // We write the report and report status. Moreover, we log the possible error, to be sure it will not be missed.
    writeReport(workDir, run, report, progressReporter.onGraphComplete(run))
    lastError.foreach(e => logger.error(s"Error while executing run ${run.id}", e))

    // Last verification that all nodes had a chance to run.
    if (report.successful && report.nodeStats.size != workflow.graph.size) {
      val missing = workflow.graph.nodes.map(_.name).diff(report.nodeStats.map(_.name))
      throw new IllegalStateException(s"Some nodes were never executed: ${missing.mkString(", ")}")
    }

    report
  }

  private def writeReport(workDir: Path, run: Run, report: RunReport, f: => Unit) = {
    try {
      repository.write(workDir, run.copy(report = Some(report)))
    } catch {
      case NonFatal(e) => logger.error(s"Unable to write report for run ${run.id}", e)
    }
    try {
      f
    } catch {
      case NonFatal(e) => logger.error("Error while reporting progress", e)
    }
  }

  private def execute(runId: String, seed: Long, params: Map[String, Any], node: Node, outputs: Map[Reference, Artifact], workDir: Path): Map[Reference, Artifact] = {
    val operator = createOp(node)
    execute(operator, params, seed, runId: String, workDir, node, outputs)
  }

  private def execute[In, Out](operator: Operator[In, Out], params: Map[String, Any], seed: Long, runId: String, workDir: Path, node: Node, outputs: Map[Reference, Artifact]): Map[Reference, Artifact] = {
    val in = createInput(node, params, outputs).asInstanceOf[In]
    val maybeSeed = if (operator.isUnstable(in)) Some(seed) else None
    val ctx = new OpContext(maybeSeed, workDir.resolve("data").resolve(s"$runId-${node.name}"), node.name)
    val out = operator.execute(in, ctx)
    extractArtifacts(node, out)
  }

  private def createOp(node: Node): Operator[_, _] = {
    val opMeta = opRegistry(node.op)
    opMeta.defn.deprecation.foreach { deprecation =>
      logger.warn(s"Using a deprecated operator ${opMeta.defn.name}: $deprecation")
    }
    injector.getInstance(opMeta.opClass)
  }

  private def createInput(node: Node, params: Map[String, Any], outputs: Map[Reference, Artifact]): Any = {
    val opMeta = opRegistry(node.op)
    opMeta.inClass match {
      case None => Unit.box(Unit)
      case Some(inClass) =>
        val ctorArgs = opMeta.defn.inputs.map { argDef =>
          node.inputs.get(argDef.name) match {
            case None => throw new IllegalArgumentException(s"Missing input: ${node.name}/${argDef.name}")
            case Some(ValueInput(value)) => value
            case Some(ReferenceInput(ref)) => if (argDef.isOptional) Some(outputs(ref).value) else outputs(ref).value
            case Some(ParamInput(paramName, defaultValue)) =>
              val v = params.get(paramName).orElse(defaultValue)
              requireState(v.isDefined, s"There should be either a $paramName param or a default value for ${node.name}/${argDef.name}")
              v.get
          }
        }
        inClass.getConstructors.head.newInstance(ctorArgs.map(_.asInstanceOf[AnyRef]): _*)
    }
  }

  private def extractArtifacts(node: Node, out: Any): Map[Reference, Artifact] = {
    val opMeta = opRegistry(node.op)
    opMeta.outClass match {
      case None => Map.empty
      case Some(outClass) =>
        opMeta.defn.outputs.map { argDef =>
          val value = outClass.getMethod(argDef.name).invoke(out)
          Reference(node.name, argDef.name) -> Artifact(s"${node.name}/${argDef.name}", argDef.kind, value)
        }.toMap
    }
  }
}