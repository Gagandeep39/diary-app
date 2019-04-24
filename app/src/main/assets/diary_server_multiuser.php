<?php
header('Content-type: application/json');
function array_push_assoc($array, $key, $value){
    $array[$key] = $value;
    return $array;
}
$user_name = "id8716954_root";  //username 
$password = "123456";   //password
$server = "localhost";  //IP
$db_name = "id8716954_contactsdb";  //database name
$data = json_decode(file_get_contents('php://input'), true);    //decode json data and store in a variable
$size = count($data);   //count the size of data array
$username = $data[0]['username'];   //used to know username, since one array of data will always belong to same user
$resultArray = array(); //initialize array
$response = "response"; //test string
$file = "ex.txt";   //file in which data is stored
$handle = fopen($file, 'w');    //open file in write mode
$con = mysqli_connect($server, $user_name, $password, $db_name);    //connect to server
if($con){   
    for ($i = 0; $i < $size; $i++) {    //to perform insert/update for each element in array list with $i counter
        $userid =  $data[$i]['_id'];
        $title = $data[$i]['title'];
        $description = $data[$i]['description'];
        $updatedOn = $data[$i]['updatedOn'];
        $deleteStatus = $data[$i]['deleteStatus'];
        $updateQuery = "UPDATE diary SET title = '$title', description = '$description', updatedOn = '$updatedOn', deleteStatus = $deleteStatus WHERE userid = $userid AND username = '$username';";
        $insertQuery = "INSERT INTO diary (id, title, description, updatedOn, username, deleteStatus, userid) VALUES ";
        $insertQuery .= "(NULL, '$title', '$description', '$updatedOn', '$username', $deleteStatus, $userid )";
        $updateResult = mysqli_query($con, $updateQuery);
        if(mysqli_affected_rows($con)>0){   //append to response array of successfully updated items
            $resultArray = array_push_assoc($resultArray, "$title", "updated");
        }
        else{   //append to response array successfully inserted items
            $insertResult = mysqli_query($con, $insertQuery);
            $resultArray = array_push_assoc($resultArray,  "$title", "inserted");
        }

     
    }  
    //check if a delete request is sent (*****use a different file for performing delte later on*****, **can be ignored since actual deleteion never happens in mysql database)
    $deleteQuery = "DELETE FROM diary WHERE deleteStatus = 1;";
    $deleteResult = mysqli_query($con, $deleteQuery);
    if(mysqli_affected_rows($con)>0)
        $resultArray = array_push_assoc($resultArray, "deleteresponse", "deleted");
    else
        $resultArray = array_push_assoc($resultArray, "deleteresponse", "no row deleted");
}
else{
    $resultArray = array_push_assoc($resultArray, "connection_response", "errorconnection");    //send reponse saying error in connection
}
mysqli_close($con);

foreach ($resultArray as $index => $string) {
        fwrite($handle, $index.':'.$string);
        $stringbreak = "\n";
        fwrite($handle, $stringbreak);
    }
            fclose($handle);
echo json_encode(array($resultArray));
?>