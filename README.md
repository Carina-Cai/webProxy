# WebProxy

A web-proxy sits between a web-client and a web-server, transmitting and filtering the information travels between the two parties. 
This self-defined web proxy server has the following functionalities:

  - listening to the user requests and send them through to the web-server;
  - caching contents for quick reponse if the same request is made by a later client (web browser);
  - filtering contents so that websites in the blocked list are not allowed to be visitied;
  - replaceing specific contents retrieved from the website with something else we defined.

## Steps to run

### The following are instructions to run the web-proxy:

Compile WebProxy.java.

``javac WebProxy.java``

Run WebProxy.class with a specifying port number, e.g., 4600.

``java WebProxy 4600``

Input the website(s) that you want to block and separate them with space, e.g., www.anu.edu.au and www.github.com. Press enter to continue. Otherwise, if you don't want to block any website, just press enter.

``www.anu.edu.au www.github.com``

Open a web browser and point to the destination website, e.g., www.baidu.com.

``http://localhost:3310/www.baidu.com``

## Limitations

This web-proxy may not be able to display all contents from any chosen website due to restrictions on the user agents.
