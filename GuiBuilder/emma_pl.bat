javac -d emma_out_pl -g -classpath lib/;../lib/jasperreports-0.6.6.jar;../lib/junit.jar;../lib/GLUE.jar;../lib/log4j-1.2.8.jar;../lib/commons-pool-1.2.jar;../lib/commons-dbcp-1.2.1.jar src/de/jdataset/*.java src/de/pkjs/pl/*.java src/de/pkjs/util/Convert.java src/de/pkjs/util/TraceUtils.java test/de/guibuilder/test/jdataset/*.java test/de/pkjs/pltest/*.java test/de/guibuilder/test/utils/*.java
pause

java emma instr -d emma_outinstr_pl -ip emma_out_pl
del emma_outinstr_pl\de\pkjs\pltest\* /Q
rmdir emma_outinstr_pl\de\pkjs\pltest /Q
del emma_outinstr_pl\de\guibuilder\test\jdataset\* /Q
rmdir del emma_outinstr_pl\de\guibuilder\test\jdataset\ /Q
del emma_outinstr_pl\de\guibuilder\test\utils\* /Q
rmdir emma_outinstr_pl\de\guibuilder\test\utils\ /Q
rmdir emma_outinstr_pl\de\guibuilder\test\ /Q
rmdir emma_outinstr_pl\de\guibuilder\ /Q

java -cp emma_outinstr_pl;emma_out_pl;lib/;../lib/mysql-connector-java.jar;../lib/junit.jar;../lib/GLUE.jar;../lib/log4j-1.2.8.jar;../lib/commons-pool-1.2.jar;../lib/commons-dbcp-1.2.1.jar;../lib/commons-collections-3.0.jar;../lib/junit.jar de.pkjs.pltest.AllTests
java emma report -r txt,html -in coverage.em -in coverage.ec -sp src
pause

rem -java.jar;../lib/junit.jar;../lib/GLUE.jar;../lib/log4j-1.2.8.jar;../lib/commons-pool-1.2.jar;../lib/commons-collections-3.0.jar;emma_out_pl de.pkjs.pltest.AllTests

