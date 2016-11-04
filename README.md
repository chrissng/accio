About this repository
=====================

[![Build Status](https://travis-ci.com/pvcnt/location-privacy.svg?token=iq89JpmicdUts87rvWPk&branch=master)](https://travis-ci.com/pvcnt/location-privacy)

This repository contains the code of all experiments ran during my PhD thesis.
It provides several modules, some of them that could be reused outside of the context of my thesis, some of them tightly related to what I am doing, i.e., location privacy.
It does *not* contain the source code of my papers.


Repository organisation
-----------------------
This repository mainly contains code related to two distinct, although sometimes interleaved, software projects: Accio and Priva'Mov.

Accio is a research oriented tool that has been used to generate results for paper we wrote.
Documentation about Accio and location privacy is available at https://pvcnt.github.io/location-privacy/

[Priva'Mov](http://liris.cnrs.fr/privamov/) is a project whose goal was to collect mobility data in order to study privacy.
This repository contains some software that has been developped in the framework of this project, such as a visualization tool that also acted as a demonstrator.  


Appendix: My thesis' topic
--------------------------
The goal of this thesis is to study the threats related to the increasing amount of geolocated data.
A first problem is the conception of privacy-preserving geolocated systems.
Actual technologies (e.g. bluetooth, WiFi) rely on protocols leaking sensitive pieces of information about their users.
This can lead to the identification of important places for people (home, work, etc.), prediction of future movements, learning of journeys’ semantic or inference of social relationships.

A second problem is the conception of privacy-preserving data dissemination system in a mobility context.
The architecture provided by the traces collecting system deployed in the Priva'Mov project, to which this thesis is related, will allow to test the efficiency of proposed solutions in a real environment.

A third problem is the investigation of privacy-preserving access mechanisms to mobility traces.
We want to propose efficient techniques to anonymize and access sequential data while offering strong anonymity guarantees.
This requires to find a trade-off between data utility for data analysts and privacy protection for people appearing in traces.