regex-ssm
=========
![CI](https://github.com/nhomble/regex-ssm/workflows/Java%20CI%20with%20Maven/badge.svg)

An attempt to demo [spring state machine](https://docs.spring.io/spring-statemachine/docs/current/reference/) with some
finite automata (regex evaluator).. and then I ended up writing more automata related code than state machine.

The parsing logic follows eliben's [work](https://github.com/eliben/code-for-blog/tree/master/2009/regex_fsm) since I wasn't
trying to redo my college project again.

# Usage
```shell script
$ java -jar regex-ssm-1.0-SNAPSHOT.jar
.
.
rex> help
.
.
Regex Commands
        regex: parse provided regex

Regex Match Commands
      * check: check if provided string matches previously provided regex
.
.
rex> regex (a|b)*cc?
rex>check aaaabbbbcc
MATCHES
rex>check c
MATCHES
rex>check d
DOES NOT MATCH
```