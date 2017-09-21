# CodeSearch

Steps to compile and execute the code

*Extract the zip and navigate to the directory
*cd codesearch
*mvn clean install (give some time for maven to fetch the dependencies)
*java -jar target\codesearch.jar

This will start the codesearch service at port 8090.
(When you do start it for the first time, it will take a while because it downloads the code from the git repo to local filesystem)

Health Check API for the search service -> http://localhost:8090/health

In order to search for a keyword/phrase, hit the url -> http://localhost:8090/search
enter the keyword to be searched against query param 'query' in the GET request.

A sample search request like -> "http://localhost:8090/search?query=abc"  fetches you the following response :

@@@@@@@@@@ Found 5 results in 4 files in 9ms while searching for { abc } @@@@@@@@                           <- search result stats

########### File - haproxy\contrib\netsnmp-perl\cacti_data_query_haproxy_backends.xml ############          <- file name

50 			<hash_110013abc35ade0aae030d90f817dfd91486f4>                                                   <- relevant code snippet with line no.s
51 				<name>HAProxy Backend Traffic</name>
52 				<graph_template_id>hash_000013b6d238ff2532fcc19ab498043c7c65c2</graph_template_id>
53 				<rrd>
54 					<item_000>
55 						<snmp_field_name>beBOut</snmp_field_name>
56 						<data_template_id>hash_010013a63ddba34026d2c07d73c0ef2ae64b54</data_template_id>
57 						<data_template_rrd_id>hash_0800136c0e4debeb9b084231d858faabd82f8f</data_template_rrd_id>
58 					</item_000>
59 					<item_001>
#######################

########### File - haproxy\src\trace.c ############

149 static char *emit_hex(unsigned long h, char *out)
150 {
151 	static unsigned char hextab[16] = "0123456789abcdef";
152 	int shift = sizeof(h) * 8 - 4;
153 	unsigned int idx;
#######################
.
.
.
.
############## End of Results ! ###########