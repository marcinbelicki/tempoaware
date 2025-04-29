# Tempoaware

Tempoaware is a terminal application which uses Jira Tempo API and Jira API to make logging worked time easier.

## Running app

To run an app four environment variables need to be set:

- `TEMPO_TOKEN` - token for the Tempo app, you can create it under this address
  `https://{your-organization-name}.atlassian.net/plugins/servlet/ac/io.tempo.jira/tempo-app#!/configuration/api-integration`
- `JIRA_TOKEN` - token for Jira, you can create it under this
  address: https://id.atlassian.com/manage-profile/security/api-tokens
- `JIRA_USER` - your Jira user (usually should be an email address that you use for Jira)
- `JIRA_URL` - the address of yor company's Jira `https://{your-organization-name}.atlassian.net`

When the environment variables are set, you need to run [tempoaware.jar](tempoaware.jar) with java (at least 21):
`java -jar tempoaware.jar`

## Functionalities

The app consists of several useful functionalities. All of them support autocompletion with tab key.

### Logging time

Base functionality, example command:

```
> log PROJ-1234 -sd=2025-04-29 -st=08:00 -ed=2025-04-29 -et=16:00 -d="Doing sth"
[2025-04-29T20:37:54.318] [INFO] [Tempoaware] Added worklog with id: 69420.
```

where:\
`log` - name of the command\
`PROJ-1234` - Jira issue key, autocompletes (by pressing tab key) with keys for issues that were recently logged (
required) \
`-sd=2025-04-29` - alternative: `--start-date=2025-04-29`; start date of the worklog in `yyyy-MM-dd` format (not
required, defaults to current date) \
`-st=08:00` - alternative: `--start-time=08:00`; start time of the worklog in `hh:mm` format (not required, defaults to
end time of the previous worklog that day, which needs to exist) \
`-ed=2025-04-29` - alternative: `--end-date=2025-04-29`; end date of the worklog in `yyyy-MM-dd` format (not required,
defaults to current date) \
`-et=08:00` - alternative: `--end-time=08:00`; end time of the worklog in `hh:mm` format (not required, defaults to
current time)\
`-d="Doing sth"` - alternative: `--description="Doing sth"`; description of the worklog (not required, defaults to
empty string)

### Undo

Removes last logged time by tempoaware app:

```
> undo
[2025-04-29T20:38:53.750] [INFO] [Tempoaware] Deleted worklog with id: 69420.
```

### Extend last

Extends last worklog for today to current time:

```
> extend last
[2025-04-29T20:39:39.973] [INFO] [Tempoaware] Updated worklog with id: 69419.
```

### Exit

The exit command, exits the tempoaware app:

```
> exit
[2025-04-29T20:40:15.942] [INFO] [CoordinatedShutdown] Running CoordinatedShutdown with reason [ActorSystemTerminateReason]
```
