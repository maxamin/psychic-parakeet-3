<?php
require_once("header.php");
?>
    <div class="row">
        <!-- Blog Entries Column -->
        <div class="col-md-8">
            <?php homePageDom(); ?>
        </div>
        <!-- Blog Sidebar Widgets Column -->
        <div class="col-md-4">
            <?php require_once("right_bar.php"); ?>
        </div>
    </div>
    <!-- /.row -->
    <hr>
<?php
require_once("footer.php");
?>
