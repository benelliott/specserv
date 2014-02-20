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

      google.maps.event.addDomListener(window, 'load', initialize);

      function initialize() {
        var mapOptions = {
          center: new google.maps.LatLng(50, 0),
          zoom: 3
        };

        map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
        
        if (markers != undefined) {
          for (var i = 0; i < markers.length; i++) {
            addMarker(markers[i][0], markers[i][1], markers[i][2]);
          }
        }
      }


      function addMarker(id, latitude, longitude) {
        var marker = new google.maps.Marker({
          position: new google.maps.LatLng(latitude, longitude),
          map: map
        });

        var audio = document.createElement('audio');
        audio.setAttribute('src','captures/'+id+'.wav');
        audio.setAttribute('id','audio'+id);
        document.getElementsByTagName('body')[0].appendChild(audio);

        var info = new google.maps.InfoWindow({
          content: '<img src = "captures/'+id+'.jpg" />'
           + '<br>'
           + '<button id="play" onClick="playAudio('+id+');">Play</button>'

        });

        google.maps.event.addListener(marker, 'click', function() {
            info.open(map,marker);
        });
      }

      function playAudio(id) {
        console.log('Playing audio ' + id);
        var audio = document.getElementById('audio'+id);
        audio.play();
      }

    </script>
  </head>
  <body>
    <div id="title-bar">
      <p id="title"> SpectroSearch </p>
      <form method="post" action="" id="search">
        <input name="species" type="text" size="40" <?php echo (isset($_POST['species']))? "placeholder='".$_POST['species']."'" : "placeholder='Search for a species...'" ?> ; />
      </form>
    </div>
      
      <?php
        if (isset($_POST['species'])) {
          echo "<div id='species_output'>";
          echo "<p id='result'>";
          $species_query = strtolower($_POST['species']);
          $con = mysqli_connect("localhost","specserv","specserv","specschema");

          if (mysqli_connect_errno()) {
           echo "Something went wrong! Please try searching again.";
          }
          $query = $con->prepare('SELECT idcaptures,latitude,longitude FROM Captures WHERE species=?');
          $query->bind_param('s',$species_query);
          $query->execute();
          $result = $query->get_result();
          $result_rows = mysqli_num_rows($result);
          if ($result_rows == 0) {
            echo "No results found for the <b>".$species_query."</b>. Please try a different species.";
          }
          else {
            echo "The <b>".$species_query."</b> has been spotted in the following locations:";
            echo "<script> markers = new Array(".$result_rows."); </script>";
            $item = 0;
            while ($row = mysqli_fetch_array($result)) {
              $id = $row['idcaptures'];
              $lat = $row['latitude'];
              $long = $row['longitude'];
              echo "<script> var coord = new Array(3); coord[0] = ".$id."; coord[1] = ".$lat."; coord[2] = ".$long."; markers[".$item."] = coord; </script>"; 
              $item++;
            }
          }
          
          mysqli_close($con);
          echo "</p>";
          echo "</div>";
      }
      ?>

    <div id="map-canvas"/>
  </body>
</html>