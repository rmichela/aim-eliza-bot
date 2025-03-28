# JavaBot

Copyright (C) 2003 Ryan Michela

## Introduction

JavaBot is a chat bot for the AOL Instant Messenger system. It is designed to attempt to engage in conversation with people via IM. The bot supports such features as remote administration and full conversation logging. There is also a mechanism for eavesdropping on conversations other people are having with the bot. All administration is handled over IM, thus allowing remote administration.

## Manifest

- **Bot.jar** - The compiled bot
- **script** - The bot script file
- **StartBot.bat** - A convenient way to start the bot from a Windows command prompt
- **StartBot.csh** - A convenient way to start the bot from a TCSH or BASH prompt
- **/source** - The source directory

## Installation/Execution

First, a working version of the Java Runtime Environment is required. If you do not have a working copy of Java, visit [java.sun.com](https://java.sun.com) or your OS manufacturer's website. If you are reading this file, you have completed the installation.

To run the bot, navigate via command line to the bot's directory and type:

```bash
java -jar Bot.jar SN PW
```

Substitute your AIM account screen name and password for `SN` and `PW`, respectively. Alternatively, you can modify the commands in `StartBot.bat` or `StartBot.csh` to start the bot with a single command.

## Administration

Once the bot has started, you administer it by sending it instant messages. The following is a list of commands you can send the bot:

```bash
#! = display this message
#!kill = kill server
#!attack SN = initiate conversation with SN
#!send SN message = interject message into conversation with SN
#!list = list active conversations
#!bind SN = watch conversation with SN
#!unbind SN = stop watching conversation with SN
#!warn = display the bot's current warning level
#!slap SN = warn SN
#!reauthent SN PW OldPW = reauthenticate the bot using SN and PW.
                          OldPW is the password of the current login
```

**Note:** `#!bind` and `#!unbind` require the exact capitalization listed on the AIM server. Spaces are substituted with an underscore. Use the `#!list` command to get the exact screen names.

## Logs

The bot creates a ton of logs, all of which end in the `.botlog` extension.

- **TOClog.botlog** contains a recount of all the bot's internal activities. This information is also displayed at the command prompt during execution.
- **xnone.botlog** contains all of the messages the bot was unable to identify. Use this file when expanding the bot's vocabulary.
- **Conversation logs** contain a recount of all conversations with the given person for the given date.

## Scripting

The bot has a relatively simple scripting system based on pre-processor commands, post-processor commands, synonym lists, and transformation rules.

### Pre-Processor Commands

```bash
pre: word substitute
```

Where `word` is the word you want substituted with `substitute`.

### Post-Processor Commands

```bash
post: word substitute
```

### Synonym Lists

```bash
synon: word synonym synonym ...
```

### Rules

Rules are a bit more complex. They are generally of the syntax:

```bash
key: keyword rank-int
    decomp: decomposition-rule
        reasmb: reassembly-rule
```

- The `key:` block represents a rule. `keyword` is the keyword that the rule references. A rule will be referenced if its keyword is found in the input string.
- `rank-int` is the rank of the rule. The highest-ranked rule in a set of matches is chosen to process the input.

#### Decomposition Rules

The `decomp:` block represents a decomposition rule that will be matched against the input string.

- Decomposition rules are regular expressions made up of words and asterisks.
- Asterisks `*` take the place of zero or more words in the expression.
- Words can be prefixed with `@` to denote that synonyms for the word should be accepted per the synonym lists.
- All decomposition rules must end with an asterisk, although a lone asterisk is acceptable.
- Multiple decomposition rules are acceptable in one `key:` block.

#### Reassembly Rules

The `reasmb:` block represents the reassembly rules.

- Reassembly rules are sentences with wildcards.
- Wildcards are in the form of numbers in parentheses that represent parts of the input that fill in the asterisks or synonyms of a decomposition rule.
- Example: `(1)` represents the part of the input string that fills the first asterisk in the decomposition rule.
- Reassembly rules can also contain a `goto` statement, which tells the bot to process another `key:` rule as if it had been selected initially.

Example:

```bash
reasmb: goto someKey
```

Multiple reassembly rules are allowed in a `decomp:` block.

### Memory

The bot also supports limited memory. If it cannot find a matching key for an input string, it processes the string in memory (if available).

To have the bot store a string in memory, insert a `$` before the regular expression in a `decomp:` rule.

Example of a complete `key:` block:

```bash
key: i 1
    decomp: * i @belief i *
        reasmb: Do you really think so?
        reasmb: Are you sure you (3)?
        reasmb: Do you really doubt you (3)?
    decomp: * i* @belief *you *
        reasmb: goto you
    decomp: $ * i am *
        reasmb: How long have you been (2)?
        reasmb: Is it normal for you to be (2)?
        reasmb: Do you enjoy being (2)?
        reasmb: Do you know anyone else who is (2)?
```

### Quit and Final Statements

- The `quit:` statement specifies what inputs will end a conversation with the bot.
- The `final:` statement tells the bot what to say when it ends a conversation.

Example:

```bash
quit: word
final: closing statement
```

Multiple `quit:` statements are acceptable, but only the last `final:` statement will be used.

## Acknowledgments

- **Jeff Heaton** for his LGPL JavaTOC library: [http://www.jeffheaton.com](http://www.jeffheaton.com)
- **Charles Hayden** for his Eliza program: [http://chayden.net](http://chayden.net)  
