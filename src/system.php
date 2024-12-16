<?php
$ip = "127.0.0.1";
$community = "public";

// Fetch sysContact, sysName, and sysLocation
$sysContact = snmp2_get($ip, $community, ".1.3.6.1.2.1.1.4.0");
$sysName = snmp2_get($ip, $community, ".1.3.6.1.2.1.1.5.0");
$sysLocation = snmp2_get($ip, $community, ".1.3.6.1.2.1.1.6.0");

$data = array(
    'sysContact' => $sysContact,
    'sysName' => $sysName,
    'sysLocation' => $sysLocation
);

echo json_encode($data);
?>
