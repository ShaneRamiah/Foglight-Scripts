// Script to check if there was hits within a specified time slice during business hours. 
// Can be use if a rule to fire alarms 

   def timeSlice = 15 // Set to 15 minutes
   def hitFilterMetric = "hitFilterNameHere"; // Hit Filter metric name
           
   def hits = 0
   def now = new Date()
   def endTime = (now).toTimestamp()   
   def startTime = (now).toTimestamp()   
   startTime.setMinutes(startTime.getMinutes()-timeSlice)
   
def hitMetrics = server.get("QueryService").queryTopologyObjects("FxVStateMetric where name like '" + hitFilterMetric + "'").toArray()

if (hitMetrics.size() > 0)
	hitMetrics.each{hits += server.DataService.retrieveAggregate(it, "countValue", startTime, endTime).getValue().max}

if (hits == 0 && (endTime.getHours() < 18 && endTime.getHours() > 8)) // Business hours 08:00 - 18:00
	return true;
else
	return false;
