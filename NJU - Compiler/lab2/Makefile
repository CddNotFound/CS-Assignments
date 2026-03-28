include Makefile.git

export CLASSPATH=/usr/local/lib/antlr-*-complete.jar

DOMAINNAME = oj.compilers.cpl.icu
ANTLR = java -jar /usr/local/lib/antlr-*-complete.jar -listener -visitor -long-messages
JAVAC = javac -g
JAVA = java


PFILE = $(shell find . -name "SysYParser.g4")
LFILE = $(shell find . -name "SysYLexer.g4")
# JAVAFILE = $(shell find . -name "*.java")
JAVAFILE = src/*.java
ANTLRPATH = $(shell find /usr/local/lib -name "antlr-*-complete.jar")

compile: antlr
	$(call git_commit,"make")
	rm -rf src/.antlr
	mkdir -p classes
	$(JAVAC) -classpath $(ANTLRPATH) $(JAVAFILE) -d classes

run: compile
	java -classpath ./classes:$(ANTLRPATH) Main $(FILEPATH)


antlr: $(LFILE) $(PFILE) 
	$(ANTLR) $(PFILE) $(LFILE)


test: compile
	$(call git_commit, "test")
	rm -f ./tests/nohup*.out
	nohup java -classpath ./classes:$(ANTLRPATH) Main ./tests/test1.sysy > ./tests/nohup1.out &
	nohup java -classpath ./classes:$(ANTLRPATH) Main ./tests/test2.sysy > ./tests/nohup2.out &
	nohup java -classpath ./classes:$(ANTLRPATH) Main ./tests/test3.sysy > ./tests/nohup3.out &
	nohup java -classpath ./classes:$(ANTLRPATH) Main ./tests/test4.sysy > ./tests/nohup4.out &
	nohup java -classpath ./classes:$(ANTLRPATH) Main ./tests/test5.sysy > ./tests/nohup5.out &


clean:
	rm -f src/*.tokens
	rm -f src/*.interp
	rm -f src/SysYLexer.java src/SysYParser.java src/SysYParserBaseListener.java src/SysYParserBaseVisitor.java src/SysYParserListener.java src/SysYParserVisitor.java
	rm -rf classes
	rm -rf out
	rm -rf src/.antlr


submit: clean
	git gc
	bash submit.sh


.PHONY: compile antlr test run clean submit


