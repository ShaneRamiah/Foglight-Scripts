import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import com.quest.wcf.core.timerange.impl.CustomTimeRange;
 
def host = args[0];
def timeRange = args[1]
def type = args[2];
def metricType = args[3];


def cal = Calendar.getInstance();
def specTimeRange = timeRange.createTimeRange(cal);
 
def format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

def start = specTimeRange.getStart();
def end = specTimeRange.getEnd();
 
startTime = new Timestamp(specTimeRange.start.getTime());
endTime = new Timestamp(specTimeRange.end.getTime());
step = 1000*60*60*9;    // 9 hours
ret = 0;


def origStart = new Timestamp(startTime.getTime());

def getMetricsFor(aInstance) {
    def metrics = [];
    def windowStart = new Timestamp(startTime.getTime());
    def windowStop = new Timestamp(startTime.getTime()+step);
    metrics.add(getMetricsFor(aInstance, windowStart, windowStop));
    windowStart.setTime(windowStop.getTime());
    windowStop.setTime(windowStop.getTime()+step);
    return metrics;
}

def getMetricsFor(aInstance, windowStart, windowStop) {
    def ds = server["DataService"];
    def metric = [];
 	metricName = metricType;
    parentIndex = metricName.lastIndexOf("/");
    if (parentIndex >= 0) {
        metricParent = aInstance.get(metricName.substring(0, parentIndex));
        metricName = metricName.substring(parentIndex+1);
    }
    else {
        metricParent = aInstance;
    }
    if (metricParent != null) 
        if (type == 1) // Average
            metric = ds.retrieveAggregate(metricParent, metricName, windowStart, windowStop)?.getValue()?.getAvg();
        else if (type == 2) // Max
            metric = ds.retrieveAggregate(metricParent, metricName, windowStart, windowStop)?.getValue()?.getMax();
        else if (type == 3) // Min
            metric = ds.retrieveAggregate(metricParent, metricName, windowStart, windowStop)?.getValue()?.getMin();

    return metric;
}

try {
	def expMetrics = [];
	while (startTime < endTime) {
	    expMetrics.addAll(getMetricsFor(host));

		cal.setTime(startTime);
		if (cal.get(Calendar.DAY_OF_WEEK) == 6) 
	    	startTime = startTime = startTime.plus(3);
	    else 
	    	startTime = startTime = startTime.plus(1);
	}

	if (type == 1)
	{
		def sum=0;
		expMetrics.each { expMetric -> 
			sum = sum + expMetric;
		}		

		return (sum/expMetrics.size());

	}
	else if (type == 2)
	{
		return expMetrics.max();
	}
	else if (type == 3)
	{
		return expMetrics.min();
	}

}
catch(e) {
	return "No Date Found";
} 
