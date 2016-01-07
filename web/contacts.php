<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>PScout</title>

    <!-- Bootstrap -->
    <!-- Latest compiled and minified CSS -->
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="components/styles/pscout.web.css" >
    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>
  <body>
    <?php 
		$section = "contacts";
		include('components/header.php'); 
	?>
	
	<div class="container main-container">
		<div class="row">
			<div class="col-md-12">
				<div class="page-header">
					<h2>Contacts <small>University of Toronto</small><h2>
				</div>
				<div class="page-header">
					<h3>Professor<h3>
				</div>
				<div class="col-md-2 contact">
					<h4><a href="http://www.eecg.toronto.edu/%7Elie">David Lie</a></h4>
				</div>
			</div>
			<div class="col-md-12">
				<div class="page-header">
					<h3>Members</h3>
				</div>
				<div class="col-md-2 contact">
					<h4><a href="mailto:kathy.au@utoronto.ca">Kathy Au</a></h4>
				</div>
				<div class="col-md-2 contact">
					<h4><a href="mailto:z.huang@utoronto.ca">James Huang</a></h4>
				</div>
				<div class="col-md-2 contact">
					<h4><a href="mailto:billy.zhou@utoronto.ca">Billy Zhou</a></h4>
				</div>
				<div class="col-md-2 contact">
					<h4><a href="mailto:ding.zhu@mail.utoronto.ca">Ding Zhu</a></h4>
				</div>
			</div>
		</div>
	</div>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <!-- Latest compiled and minified JavaScript -->
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
  </body>
</html>