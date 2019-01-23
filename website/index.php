<?php
session_start();
if(isset($_SESSION["username"])){
  if($_SESSION["expires_in"] < time()){
    unset($_SESSION["username"]);
    unset($_SESSION["expires_in"]);
  }
  else
    http_redirect("home");
}
?>
<!DOCTYPE html>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Login</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" type="text/css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js" type="text/javascript">
</script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" type="text/javascript">
</script>
    <style type="text/css">
body {
        background-image: url("assets/background.jpg");
        background-size: cover;
    }
        .login-form {
                width: 340px;
        margin: 50px auto;
        }
    .login-form form {
        margin-bottom: 15px;
        background: #f7f7f7;
        box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
        padding: 30px;
    }
    .login-form h2 {
        margin: 0 0 15px;
    }
    .form-control, .btn {
        min-height: 38px;
        border-radius: 2px;
    }
    .btn {        
        font-size: 15px;
        font-weight: bold;
    }

    img{
        /* max-height:160px; */
        max-width:290px;
        height:auto;
        width:auto;
    }

    </style>
</head>

<body>
    <div class="login-form">
        <form action="login.php" method="post">
            <img src="assets/logo4-final.png" class="img-fluid">

            <h2 class="text-center">Log in</h2>

            <div class="form-group">
                <input type="text" name="username" class="form-control" placeholder="Username" required="required">
            </div>

            <div class="form-group">
                <input type="password" name="password" class="form-control" placeholder="Password" required="required">
            </div>

            <div class="form-group">
                <button type="submit" class="btn btn-primary btn-block">Log in</button>
            </div>

            <div class="clearfix">
                <label class="pull-left checkbox-inline"><input type="checkbox"> Remember me</label> <a href="#" class="pull-right">Forgot Password?</a>
            </div>
        </form>

        <p class="text-center"><a href="#">Create an Account</a></p>
    </div>
</body>
</html>