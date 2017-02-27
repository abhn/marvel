# Marvel - Face Recognition With Android & OpenCV
Marvel is an open source android project focused on easing the task of attendance marking in schools and colleges using face recognition on Android smartphones.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/abhn/marvel.svg?branch=master)](https://travis-ci.org/abhn/marvel)
[![Documentation Status](https://readthedocs.org/projects/marvel/badge/?version=latest)](http://marvel.readthedocs.io/en/latest/?badge=latest)



## The Problem
In the schools and colleges, considerable amounts of time and energy are spent in tracking attendance of students. Biometrics are out of reach for most institutions. This problem can easily be solved with a handheld data collecting device such as a smartphone.

A solution can be designing of an application that harnesses the phone camera and internet connection to enable fast data collection, storage and drawing of actionable insights from the data.

## Introduction To Docs
The goal of this documentation is to enable users and developers make sense of what we're trying to achieve here and maybe extend this project to further suit their needs. The core of the project would always be face recognition, but in order to enable it's primary audience to used.

Link to developer documentation: [http://marvel.readthedocs.io/en/latest](Stable)

Link to user documentation: TODO

## Broad Requirements
- Attendance Marker
    - Register faculty
    - Register individual student
    - Capture attendance / Review and store results
    - Insights and analytics
    - Export results (xls, csv, sql)

## Primary Stakeholders
- Institute faculty with basic knowledge of application usage

## Assumptions
- Institute faculty has a relatively newer Android smartphone
- Phone has a rear camera to capture data
- Phone has an internet connection to sync data

## Milestones

1. Implement face recognition algorithm in a dummy app (Single face)
2. Implement face recognition algorithm for group
3. Get app structure ready for integration of algorithm
4. Reviewing attendance / Committing attendance to Firebase DB.
5. Analytics and insights section