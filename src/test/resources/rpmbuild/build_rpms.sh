#!/bin/bash

rpmbuild -ba SPECS/*.spec -D "_topdir $(pwd)"
