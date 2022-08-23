#!/usr/bin/make -f

components = $(shell ls /tmp/test_artifacts/rpms/)
tests = $(patsubst %,test-%,$(components))

all: $(tests)

test-%:
	@./ci_test.sh execute_one $*

.PHONY: all
