db.auth("admin", "admin");
var status = rs.status();
var primary = "";

for(var i=0; i<status.members.length; i++) {
	if(status.members[i].stateStr === 'PRIMARY') {
		primary = status.members[i].name;
	} 
}

if(primary.length > 0) {
	var conn = new Mongo(primary);
	var adminDb = conn.getDB("admin");
	var initialAuth = adminDb.auth("admin","admin");

	if(initialAuth === 0) {
		adminDb.addUser("admin","admin"); 
		adminDb.auth("admin","admin");  
		conn.getDB("router-dev").addUser("router","router");
	}
}
