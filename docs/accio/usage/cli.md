---
layout: accio
nav: accio
title: Command line reference
---

The principal interface of Accio is a command line interface.
It can be used to [run experiments](running.html) and [analyze their results](analysing.html).
We present here some additional features of this application.

* TOC
{:toc}

## `.acciorc`, the Accio configuration file

Accio accepts many options.
While some of them are frequently varying (e.g., the name of an experiment), some others are less susceptible to vary (e.g., your user name).
To prevent you from typing again and again the same options each time you use Accio, you can specify them once for all in a configuration file called `.acciorc`.

Accio looks for a configuration file at the path specified by the `-acciorc` option.
If not specified, Accio looks for a `.acciorc` in the current directory where launched Accio, then for `~/.acciorc` and finally for `/etc/accio.acciorc`.
The first file found will be used, the other ones, even if existing, will not be considered.
Consequently, specifying `-acciorc=/dev/null` will disable any configuration file parsing.

`.acciorc` files are text files following a simple line-based grammar.
Lines starting with a `#` are considered as comments and ignored.
Blank lines are ignored.
Each line starts with a command name then followed by a list of options that will be appended to any execution of this particular command.
It means the first word must necessarily by the name of an Accio command, such as `run` or `export`.
Many lines can be used for the same command, they will be accumulated (the last lines taking precedence over the previous ones).
Moreover, options specified through the command line always take precedence over those coming from a configuration file.

In configuration files, commands names may suffixed by `:config`, `config` being a tag for a particular configuration.
These options are ignored by default, but can be included with the `-config` option.
The goal of this is to package command line options that work together.
For example, a `run:twice -runs=2` line in a configuration file combined with an `accio run -config=twice /path/to/workflow.json` Accio invocation in running twice any workflow.

The `-acciorc` and `-config` options must appear *before* the command name (e.g., `run`).

## Built-in documentation

Accio comes with built-in documentation.

  * `accio help` enumerates all available commands and their options.
  * `accio help <command>` provides detailed information about a given command.
  * `accio help list-ops` enumerates all available operators, their inputs and outputs.
  * `accio help <operator>` provides detailed information about a given operator.