# Overview

This is ported from [mxunit-ant](https://github.com/marcesher/mxunit-ant) to
support [testbox](https://www.ortussolutions.com/products/testbox).

## Minimum Requirements

Java version 1.7

## Issues

1. When deployed in GoCD, `java.net.SocketTimeoutException` is thrown.
 Setting the timeout to infinite solved the problem. Alternatively, setting
 to 10 seconds is still an issue despite the test appears to run really fast.
