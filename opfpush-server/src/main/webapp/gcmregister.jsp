<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>

  <head>
    <link rel='icon' href='favicon.png'/>
  </head>

  <body>
	Insert device Registration_id:
  	<form action="/gcmregister" method="post">
		<div><textarea name="txtRegId" rows="3" cols="60"></textarea></div>
	    <div><input type="submit" value="Submit" /></div>
  	</form>
  </body>
</html>
