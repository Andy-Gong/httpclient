# httpclient
This project said how to handle the retry logic when the IOException happens or http status code isn't 200 in http client.
In unstable network, retry can help to increase the success api call when the network jitter easily.

## IOException
When executing HttClient methods, it may throw two type exceptions, transport exceptions and protocol exceptions. 
### Transport exceptions
Transport exceptions are caused by input/output failures such as an unreliable connection or an inability to complete the execution of an HTTP method within the givin time constraint.
Gnerally transport exceptions are non-fatal and maybe recovered from by retrying the failed method. Transport exceptions includes belows.
#### SocketException
#### NoHttpResponseException
#### ConnectTimeoutException
### Protocol exceptions
Protocol exceptions generally indicate errors caused by a mismatch between the client and the server in their interpretation of the HTTP specification. 
Usually protocol exceptions cannot be recovered from without making adjustments. 
Detail see here: http://hc.apache.org/httpclient-3.x/exception-handling.html

## HttpStatusCode
Here defines all HTTP status code, https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html, 
