SHELL := /bin/bash

.SILENT:

UNAME := $(shell uname)
ROOT_DIR := $(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))


default: help

# https://news.ycombinator.com/item?id=11939200
.PHONY: help
help:	### list main targets
ifeq ($(UNAME), Linux)
	@grep -P '^[a-zA-Z_-_/]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'
else
	@# this is not tested, but prepared in advance for you, Mac drivers
	@awk -F ':.*###' '$$0 ~ FS {printf "%15s%s\n", $$1 ":", $$2}' \
		$(MAKEFILE_LIST) | grep -v '@awk' | sort
endif


DOCKER_HUB_USER ?= $(USER)
IMAGE_TAG_BASE ?= $(DOCKER_HUB_USER)/maude-bindings-build
IMAGE_VERSION ?= $(shell git rev-parse --short HEAD)
.PHONY: image/build 
image/build: 	### build build image
	echo
	echo "Building builder image ..."
	docker build -t $(IMAGE_TAG_BASE):$(IMAGE_VERSION) .
	docker image tag $(IMAGE_TAG_BASE):$(IMAGE_VERSION) $(IMAGE_TAG_BASE):latest
	echo "Done"

.PHONY: image/push
image/push: 	### publish build image
	echo 'NOTE: this assumes already logged to Docker Hub, e.g. with `docker login`'
	echo
	docker push $(IMAGE_TAG_BASE):$(IMAGE_VERSION)
	echo
	echo 'See image at https://hub.docker.com/repository/docker/$(IMAGE_TAG_BASE)'

.PHONY: clean
clean: 	### clean the build
	cd $(ROOT_DIR)/.. && \
	sudo rm -rf subprojects/maudesmc CMakeCache.txt CMakeFiles/ cmake_install.cmake dist && \
	git submodule update

.PHONY: build/java
build/java: 	### build the java bindings
	cd $(ROOT_DIR)/.. && \
	docker run -it -v $(ROOT_DIR)/..:/work -w /work $(IMAGE_TAG_BASE):latest ./build/build_java_linux.sh

.PHONY: build/shell
build/shell:	### run the build shell
	cd $(ROOT_DIR)/.. && \
	docker run -it -v $(ROOT_DIR)/..:/work -w /work $(IMAGE_TAG_BASE):latest /bin/sh
