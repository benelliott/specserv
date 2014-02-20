<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <link rel="stylesheet" type="text/css" href="style.css">
    <link href='http://fonts.googleapis.com/css?family=Offside' rel='stylesheet' type='text/css'>
    <script type="text/javascript"
      src="http://maps.googleapis.com/maps/api/js?key=AIzaSyAsbn16gcLMLoB--boRTvLcG9hNVuvPBnA&sensor=false">
    </script>
    <script type="text/javascript">


    var map;
    var markers;
      function initialize() {
        var mapOptions = {
          center: new google.maps.LatLng(50, 0),
          zoom: 3
        };
        
        map = new google.maps.Map(document.getElementById("map-canvas"),
            mapOptions);
        
        for (var i = 0; i < markers.length; i++) {
          addMarker(markers[i][0],markers[i][1]);
        }

      }
      google.maps.event.addDomListener(window, 'load', initialize);

      function addMarker(latitude, longitude) {
        console.log("Adding marker...");
        var marker = new google.maps.Marker({
          position: new google.maps.LatLng(latitude, longitude),
          map: map
        });
      }

    </script>
  </head>
  <body>
    <div id="title-bar">
      <p id="title">
        SpectroSearch
      </p>
      <form method="post" action="" id="search">
        <input name="species" type="text" size="40" placeholder="Search for a species..." />
      </form>
    </div>
    <div id="species_output">
      <p id="result">
      <?php
        if (isset($_POST['species'])) {
          $species_query = strtolower($_POST['species']);
            $con = mysqli_connect("localhost","specserv","specserv","specschema");

            if (mysqli_connect_errno()) {
             echo "Something went wrong! Please try searching again.";
            }

            $query = mysqli_query($con,"SELECT latitude,longitude FROM Captures WHERE species=\"".$species_query."\"");
            $result_rows = mysqli_num_rows($query);
            if ($result_rows == 0) {
              echo "No results found for the <b>".$species_query."</b>. Please try a different species.";
            }
            else {
              echo "The <b>".$species_query."</b> has been spotted in the following locations:";
              echo "<script> markers = new Array(".$result_rows."); </script>";
              $item = 0;
              while ($row = mysqli_fetch_array($query)) {
                $lat = $row['latitude'];
                $long = $row['longitude'];
                echo "<script> var coord = new Array(2); coord[0] = ".$lat."; coord[1] = ".$long."; markers[".$item."] = coord; </script>"; 
                $item++;
              }
            }
            
            mysqli_close($con);
        }
      ?>
      </p>
    </div>
    <div id="map-canvas"/>
  </body>
</html>