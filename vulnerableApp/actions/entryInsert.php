<?php
/*
    Piti's blog is a very vulnerable web app written to illustrate a php security training
    Copyright (C) 2012 Manuel Silvoso

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

	$text=isset($_REQUEST['text'])?addslashes(trim($_REQUEST['text'])):'';
	if (!empty($text))
	{
		$query="INSERT INTO entries (entry_text,entry_date) VALUES ('$text',now())";
		dbQuery($query, $conn);
	}
	
	header('Location:index.php');
	exit;
