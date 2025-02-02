<html>
    <head>
        <title>XSS-attacks</title>
    </head>
    <body>
        <h1> XSS-attacks </h1>
        <p id="XSSintro">This site contains the possibility for XSS-attacks</p>
        
        <?php
        if ($_POST) {
            $input = $_POST['xss'];
            echo $input . "\n";
        }
        ?>
        
        <p id="Links">
            <a href="http://localhost/apache-bench/index.html">This link leads to the homepage</a><br>
            <a href="http://localhost/apache-bench/inject.html">Here you can try SQL-Injection</a><br>
            <a href="http://localhost/apache-bench/xss.html">Here you can try XSS-Attacken</a><br>
        </p>
    </body>
</html>