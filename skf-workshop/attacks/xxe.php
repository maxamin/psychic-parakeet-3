<?php

$xml = simplexml_load_file("attacks/news.xml") or die("Error: Cannot create object");
		
echo"<h2>Latest News</h2>";
echo"<h4>".$xml->item[0]->titles."</h4>";
echo"<h5>".$xml->item[0]->date."</h5>";
echo"<p>".$xml->item[0]->tekst." ";
echo"</p>";

echo"<h2>Latest News</h2>";
echo"<h4>".$xml->item[1]->titles."</h4>";
echo"<h5>".$xml->item[1]->date."</h5>";
echo"<p>".$xml->item[1]->tekst." ";
echo"</p>";

?>