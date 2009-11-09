<html>
    <head>
        <title>XWoot Error</title>
    </head>
    <body>    
        <div id="errorPage">            
                <h3>XWoot Error</h3>
                <div id="errorExplain">
                    ${exception.message}
                </div>                    
                <hr/>
                <span class="error">
                    ${cause}
                </span>
                <pre>
<%((Exception)request.getAttribute("exception")).printStackTrace(new java.io.PrintWriter(out));%>
                </pre>
        </div>        
    </body>
</html>
