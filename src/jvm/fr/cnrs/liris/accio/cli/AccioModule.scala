package fr.cnrs.liris.accio.cli

import com.google.inject.{AbstractModule, Provides, Singleton}
import fr.cnrs.liris.accio.core.dataset.DatasetEnv
import fr.cnrs.liris.accio.core.framework.{AnnotationOpMetaReader, OpMetaReader, OpRegistry}
import fr.cnrs.liris.accio.core.ops.eval._
import fr.cnrs.liris.accio.core.ops.source.EventSourceOp
import fr.cnrs.liris.accio.core.ops.transform._
import fr.cnrs.liris.accio.core.pipeline._
import net.codingwell.scalaguice.ScalaModule

object AccioModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[OpMetaReader].to[AnnotationOpMetaReader]
    bind[ExperimentParser].to[JsonExperimentParser]
    bind[WorkflowParser].to[JsonWorkflowParser]
    bind[ExperimentExecutor].to[LocalExperimentExecutor]
    bind[ReportWriter].to[JsonReportCodec]
    bind[ReportReader].to[JsonReportCodec]
  }

  @Provides
  def providesDatasetEnv: DatasetEnv = {
    new DatasetEnv(math.max(1, sys.runtime.availableProcessors() - 1))
  }

  @Provides
  @Singleton
  def providesOpRegistry(metaReader: OpMetaReader): OpRegistry = {
    val registry = new OpRegistry(metaReader)
    // Sources
    registry.register[EventSourceOp]

    // Transformers
    registry.register[CollapseTemporalGapsOp]
    registry.register[GaussianKernelSmoothingOp]
    registry.register[MaxDurationOp]
    registry.register[MinDurationOp]
    registry.register[MinSizeOp]
    registry.register[MaxSizeOp]
    registry.register[SpatialSamplingOp]
    registry.register[DurationSplittingOp]
    registry.register[SizeSplittingOp]
    registry.register[SpatialGapSplittingOp]
    registry.register[TemporalGapSplittingOp]
    registry.register[SequentialSplittingOp]
    registry.register[TemporalSamplingOp]
    registry.register[UniformSamplingOp]
    registry.register[GeoIndistinguishabilityOp]
    registry.register[PromesseOp]

    // Evaluators
    registry.register[PoisRetrievalOp]
    registry.register[SpatialDistortionOp]
    registry.register[TemporalDistortionOp]
    registry.register[AreaCoverageOp]
    registry.register[DataCompletenessOp]
    registry.register[TransmissionDelayOp]
    registry.register[BasicAnalyzerOp]
    registry.register[PoisAnalyzerOp]

    registry
  }

  @Provides
  def providesCommandRegistry: CommandRegistry = {
    val registry = new CommandRegistry
    registry.register[RunCommand]
    registry.register[VisualizeCommand]
    registry.register[HelpCommand]
    registry.register[OpsCommand]
    registry
  }
}