
### recent thoughts

- think of questions that folks will ask
- add '~; tut; sh make -C slides' somewhere with reference to LivePage, keep entr-make.sh around?
- remove 2 dim version once example code is in slides?
- switch to spire's Interval and Point?
- kind projector
- define laws and utilize discipline
- explore the following as listed out in the cats project
    - simulacrum for minimizing typeclass boilerplate
    - machinist for optimizing implicit operators
    - discipline for encoding and testing laws
    - kind-projector for type lambda syntax
    - algebra for shared algebraic structures
    ...and of course a pure functional subset of the Scala language. (_http://i.imgur.com/a04WoHn.png_?)

### before presentation

- merge topic/presentation-prep to master and publish to slides gh-pages
- would be nice to run through with a few folks next week

### ideas, todos, notes

- look further at archery and various R-Tree implementations
    - review https://github.com/meetup/archery and R-Tree variants, e.g. http://en.wikipedia.org/wiki/M-tree
- continue with spark impl post 1st preso
- direct further work based on feedback
- spark impl
  - to demo spin up cluster or deploy to already running cluster
  - run SparkSQL against the RDD and GraphX R-Trees, perhaps in conjunction with other related RDDs
  - RDD version to motivate the need for the following GraphX version
    - compare contrast show advantages of the Property Graph version
- run k-means against all the coordinates on in the R-Tree, show the result on the map, stretch goal click on and have that utilize the R-Tree to look up associated data with that coordinate
- any other MLib examples that would be interesting or related in some fashion? this might be too much for a single presentation. better to stay focused. k-means would be the, hey this in cool you should check it out, maybe I'll show a few some additional distributed ML algorithms in a future talk.
- show a spark standard standard map-reduce sort of Job in conjunction with distributed R-Tree
- compare to other distributed R-Tree implementations?
- why spark and what is it
    - next generation "hadoop" map-reduce
    - improved performance for iterative algorithms due to caching and tachyon the in memory distributed file system (fill in, research a few details)
    - scala collections (functional-like) API, e.g. code looks similar whether it's running locally or being distributed amongst a cluster, caveat: the abstraction is leaky, but still helpful
    - quote fastest sort record (think it's been eclipsed by google, check on this)
    - tachyon over HDFS along with other compelling reasons...
- MLib annex description
- R-Tree as RDD
  - indexed RDD info interesting : http://stackoverflow.com/questions/24724786/distributed-map-in-scala-spark
  - might want to mention the above after an quick explanation of partitioning, scanning, and the desire for indexing
  - bit of discussion then onwaryd to GraphX R-Tree
- GraphX is ??? what does it do?
- distributed rtree slides : what if it doesn't fit in memory? how bout the clusters memory then.
- property graph is ??? and is good for ???
    - how does it relate to R-Tree
    - show how distributed property graph R-Tree improves over RDD R-Tree
- R-Tree → NDim-R-Tree → R-Tree as RDD → GraphX R-Tree
