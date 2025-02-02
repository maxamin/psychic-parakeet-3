## Code injection
### SQL injection
Possible in _SeparationFilterRepository_

##### Why
Parameter _filter_ is simply concatenated to the rest of the sql query.
This allows for some query manipulation if you turn filter into 

eg: _A%' OR 1=1 OR s.CREATED_USER LIKE '%A_

#### How to solve 
Parametrise the user-inputted fields. 
EG by using a NamedParameterJdbcTemplate and adding a map with parameters as second argument when doing the queryForList call.

If you want to see Spring escaping the special characters, do the following:
- Start your backend in debug mode
- Put a debug point at JdbcTemplate.query -> rs = ps.executeQuery(); (line 666) 
- In the UI, try to filter using SQL injection
- If you evaluate the line with the debug point, you will get a 'HikariProxyResultSet' object. 
This has a 'Statement' field. If you check the delegate on it, you will see that the special characters have been escaped with backslashes.

### Code injection (non-sql)
#### Why
Backend stores any user input without checking if there is any executable code in it.
The UI does the same.

Try to input the following in the argumentation column of the separation table:

<h1>This could go wrong!</h1>
<p style='background-color:red'>For example...<p>
  <style>h1 {background-color:yellow;}</style>
  
  or
  
  <style>table {
  display: none !important;
  }</style>

#### How to solve
Angular escapes all code by default (both input and variables retrieved from backend), but other frameworks might not.
It is safest to implement a mechanism in the backend to escape all input received from rest calls that can be called by users you don't trust. 

EG: use OWASP sanitizer: https://github.com/OWASP/java-html-sanitizer/blob/master/src/main/java/org/owasp/html/examples/EbayPolicyExample.java

## CSRF
#### Why
UI stores its logged in user data in a cookie. 
The backend uses this data to authenticate the user (with the current code it does not, but imagine it does).
However, this cookie is always sent to the backend, even if the request is done from another source (eg the csrf-attacker).
This allows other sites to perform valid REST calls to our backend as long as the cookie is present.

#### How to solve
A modern web-app should rely on cookies. Userdata should preferably be stored in the js.
If you want to keep this data over multiple browser sessions, use localstorage instead.

To test:
Debug in the CsrfController.

First try to perform the csrf attack with the csrf attacker. 
You will see that both the cookie and the _request.getHeader("AUTH_DATA")_ return empty.

Now open the 'Rate' page in the regular UI. This page will also call the CsrfController. 
Here, the cookie will also be empty. The AUTH_DATA header will be filled in correctly.

### Insecure direct object reference

#### Why
Separations table uses an auto-generated, incrementing primary key. 
This makes it very easy to guess the ID's of all separations by changing the url of the separation detail page

#### How to solve
Change the auto-incremented id to a UUID. This makes it very hard to guess an existing ID.

Also add some extra checks in the backend to see if a given user has the right to access the details of a separation.
This is achieved by changing the repository call 'FindById' to 'FindByIdAndCreatedUser' in the SeparationController's _GetSeparationById_ method.

### Broken access control

#### Why
The list on the 'Sepa' page performs a backend call that returns all items from the DB. 
Filtering on which items should or should not be shown is done in the UI.
A tech-savvy user could log in, perform the REST call (or inspect network traffic) and see all items.

#### How to solve
In the backend, construct the query in such a way that it only returns the items for the user that requests them.
