## Modules
  * [Information](Information.md)
  * [Shooting](Shooting.md)
  * [Projectile](Projectile.md)
  * [Damage](Damage.md)
  * [Explosion](Explosion.md)
  * [Scoping](Scoping.md)
  
## Other
  * [Permissions](CommandsPermissions.md)
  * [Protections](Protections.md)
  * [Developer API](API.md)
  * [References](References.md)
  
## Tutorials
  * [Installation]()
  * [Resource Pack Help]()

## Frequently Asked Questions
> **There is an error in the console when the plugin starts!**

This may be either because you forgot to install [MechanicsCore], *or* because you made an error in config. 
Make sure to watch [CJCrafter's WeaponMechanics installation tutorial](youtube).

This may also be an issue caused by your formatting. After you've watched the
video above, look here:

YAML follows a very strict format. Make sure you are using Notepad++ to edit `.yml`
files! Common issues:
  * Using tabs instead of spaces
  * Each node is indented correctly
  * All strings are surrounded by quotes
  
If you are still having problems, there are still a few things you can try:
  * **Check console for errors** - Chances are, either Spigot or MechanicsCore 
  are trying to tell you that you've made a mistake! Look out for lines that 
  start with `[Server thread/ERROR]: [MechanicsCore]`. This will tell you 
  exactly where your issue is.
  * **Paste your config into a validator** - A [YAML Validator](https://jsonformatter.org/yaml-validator)
  can help tell you where your issue is. Click on the link, paste 
  your config, then click "Validate YAML"
  
Now that you have tried EVERYTHING possible, you should [get in contact with us]()
so we can help fix your issue.
  
> **Can you add gun (Insert Gun Here)?**

This plugin is programmed so people can be creative and design their own weapons.
The default weapons exist to give people inspiration, not for you to build your server around.
If you do not want to make the weapons yourself, somebody may be willing to commission a weapon pack for you