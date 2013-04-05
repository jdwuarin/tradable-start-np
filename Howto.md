How this app was Written:
========================= 
In this howto, you will learn how the four main arteries of this app, namely: 

* Placing a market order and uses the client order id.
* Sending a pending order an watch for its execution
* Monitoring the change in position once an order is placed
* Making use of tracks and places orders on different tracks.

Part one: Sending a market order
---------------------------------

This first part is probably the hardest part to go through, but once you get how this was done, you will understand the other three points in a breeze. 
In order for you to be able to place an order, you will have to make use of the following App services in your code: