<?php
//This is the server code for the android app


header('Content-type: application/json');
function array_push_assoc($array, $key, $value){
    $array[$key] = $value;
    return $array;
}
$user_name = "id8716954_root";
$password = "123456";
$server = "localhost";
$db_name = "id8716954_contactsdb";
$data = json_decode(file_get_contents('php://input'), true);
$size = count($data);
$username = $data[0]['username'];
$resultArray = array();
$response = "response";
$file = "ex.txt";
$handle = fopen($file, 'w');
$con = mysqli_connect($server, $user_name, $password, $db_name);
if($con)
{   for ($i = 0; $i < $size; $i++) {
        $userid =  $data[$i]['_id'];
        $title = $data[$i]['title'];
        $description = $data[$i]['description'];
        $updatedOn = $data[$i]['updatedOn'];
        $deleteStatus = $data[$i]['deleteStatus'];
        $updateQuery = "UPDATE diary SET title = '$title', description = '$description', updatedOn = '$updatedOn', deleteStatus = $deleteStatus WHERE userid = $userid AND username = '$username';";
        $insertQuery = "INSERT INTO diary (id, title, description, updatedOn, username, deleteStatus, userid) VALUES ";
        $insertQuery .= "(NULL, '$title', '$description', '$updatedOn', '$username', $deleteStatus, $userid )";
        $updateResult = mysqli_query($con, $updateQuery);
        if(mysqli_affected_rows($con)>0){
            $resultArray = array_push_assoc($resultArray, "$title", "updated");
        }
        else{
            $insertResult = mysqli_query($con, $insertQuery);
            $resultArray = array_push_assoc($resultArray,  "$title", "inserted");
        }


    }


$deleteQuery = "DELETE FROM diary WHERE deleteStatus = 1;";
$deleteResult = mysqli_query($con, $deleteQuery);
if(mysqli_affected_rows($con)>0)
$resultArray = array_push_assoc($resultArray, "deleteresponse", "deleted");
else
$resultArray = array_push_assoc($resultArray, "deleteresponse", "no row deleted");
}
else
{
    $resultArray = array_push_assoc($resultArray, "connection_response", "errorconnection");
}
mysqli_close($con);
// ALTER TABLE tablename AUTO_INCREMENT = 1 to make autoincrement 0 after unnecessary deletion
echo json_encode(array($resultArray));
?>