
## Generic N-Dim R-Tree Explorations

_or how I learned to calm my one dimensional index woes_ (cliché?)

_or how I learned to let go of the one dimensional index_

[Dave Rostron](http://github.com/drostron) -- [`@yastero`](http://twitter.com/yastero)

Feb 17, 2015

-----

- presentation plan? perhaps depends on how long this gets
- warning: this is not complete
- just a place for some fun exploration
- spured by Data Intensive book

-----

### What if I want to index my data

- B-Tree to the rescue
 ![](http://upload.wikimedia.org/wikipedia/commons/6/65/B-tree.svg)
- _quick explanation and diagram of a btree here_

-----

### wait I want to index my data over 2 dimensions

- could get something from btree but what about range query over both dimensions?
- R-Tree to the rescue
- _quick explanation and diagram of an R-Tree_

-----

R-Tree Overview

- spatial index, n-dimensional index
- variants


> example quote
>
> -- author here

hmm, R-Trees sound useful, tell me more

<aside class="notes">
a note, is this thing on?, it doesn't update?

- hmm, notes, could be useful, the div tag seems a bit verbose but perhaps ok
- audience reaction?...
</aside>

-----

2 dim overview

-----

wait I have more that 2 dimensions to index

- <phase in> shapeless sized

- <example>

testing a code block

~~~ {.haskell}
main = putStrLn "Hello, world!"
~~~

-----

wait I have heterogeneous datatypes to index on

- <phase in> shapeless HList

- <example>

-----

- intentionally postponed a few items
  - balancing
  - performance
  - heterogeneous distance function

-----

- fun things explored
  - rtrees (but you already know that)
  - indexes
  - shapeless goodness
    - hlists, map, zip, etc operations over hlists
  - spire
    - ...
  - heterogenous distance functions
  - scalacheck ; property based testing
  - benchmarking
  - ...

-----

tools (beginning or perhaps at end in summary)

- shapeless - <description here>
- spire - <description here>
- scalacheck - property based testing
- Thyme - <description here>

-----

...

-----

caveats:

don't use this in production or if you think it is useful let's crisp up the implementation together

-----

initial performance comparisons tests:

plenty of room for improvement. just a first naive pass at the moment. if interested let's hack together.

-----

future explorations

- R-Tree variants, e.g. M-Tree, X-Tree, Hilbert R-tree
- heterogenous distance functions
- distributed spark impl
- run k-means against all the coordinates on in the R-Tree, show the result on the map, utilize the R-Tree to look up associated data with that coordinate
- specialization and/or miniboxing
