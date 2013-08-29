>> history_user_bhv

select 
	datetime(time/1000, 'unixepoch', 'localtime') as time, 
	duration,
	datetime(end_time/1000, 'unixepoch', 'localtime') as end_time,
	timezone,
	bhv_type,
	bhv_name
from history_user_bhv;


>> history_user_env

select 
	datetime(time/1000, 'unixepoch', 'localtime') as time, 
	duration,
	datetime(end_time/1000, 'unixepoch', 'localtime') as end_time,
	timezone,
	env_type,
	user_env
from history_user_env;


>> matched_result

select 
	datetime(time/1000, 'unixepoch', 'localtime') as time, 
	timezone,
	bhv_type,
	bhv_name,
	matcher_type,
	likelihood
from matched_result;
 
 
>> predicted_bhv

select 
	datetime(time/1000, 'unixepoch', 'localtime') as time, 
	timezone,
	user_envs,
	bhv_type,
	bhv_name,
	score
from predicted_bhv;