# CodeSearch
#### This is a text search engine library written in Scala and Akka. It is a custom implementation which provides fast (subsecond query time) keyword search over a git repoistory's source code. When a word or phrase is given, relevant code snippets and their corresponding source files show up as results.

Steps to compile and execute the code

* Clone the repo and navigate to the directory
* cd codesearch
* mvn clean install (give some time for maven to fetch the dependencies)
* java -jar target\codesearch.jar

This will start the codesearch service at port 8090.
(When you do start it for the first time, it will take a while because it downloads the code from the git repo to local filesystem)

Health Check API for the search service -> http://localhost:8090/health

In order to search for a keyword/phrase, hit the url -> http://localhost:8090/search
enter the keyword to be searched against query param 'query' in the GET request.
