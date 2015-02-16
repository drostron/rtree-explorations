#!/bin/sh

# works nicely in conjunction with https://github.com/MikeRogers0/LivePage

ls ../target/scala-2.11/tut/slides.md Makefile style.html | entr make
