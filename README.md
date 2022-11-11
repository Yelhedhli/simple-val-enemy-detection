# simple-val-enemy-detection

Simple program to do enemy detection in Valorant as quickly as possible **without** machine learning. 

Currently encompasses three algorithms to do object detection, respectively built on divisive hierarchical clustering, image fragmentation and local greedy search. The best approach would probably be to interlace these algorithms.




## FAQ

#### Why?

Inspired by [this](https://www.youtube.com/watch?v=LXA7zXVz8A4) video, I wanted to recreate this using a more selective implementation of ML in order to lower computational load and reach low render times needed to effectively play Valorant. The first task I started out with was the object detection which I felt could be achieved without ML at all, so I challenged myself to do it.

#### Why didn't you write this in something other than Java?

Because this is the dev environment I had set up at the time. Yes, if I were to do it again I would have used something better suited to the task like C/C++ or Rust.

#### Are there plans to finish the rest of the bot?

Not at the moment, I might come back to it eventually though.
