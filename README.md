
# JBCreditShop
Create and manage an ecobits shop. Add and remove items to purchase.

## Dependencies
- [EcoBits](https://www.spigotmc.org/resources/ecobits-⭕-create-custom-currencies-✅-improve-your-monetization-✨-supports-shops-mysql.109967/)
- [LuckPerms](https://luckperms.net/download)
## Commands
- `/creditshop||crshop` Opens the shops menu for managing. ~ **Perm: jbcreditshop.admin**
- `/creditshop reload` Reloads configs & updates the shops to the files. ~ **Perm: jbcreditshop.admin**
- `/creditshop open <shop_id>` Opens the shop menu. (if they can view it)
## Usage
There is a LOT to go through. I will try and explain everything as best as I can.


## Shop Template

<details> 

<summary>Shop Template</summary>


```yml
'1':
  shop_id: id  # Must be unique. Used in /crshop open <shop_id>
  default_price: 12000 # The default price of all items in 
  default_display_material: GOLD_INGOT # The default display material all items in this shop will have.
  shop_display: '&6Test Shop' # The title of this shop.
  discount: 20 # Percent discount to all items in store or the applied group. (0-100)
  discount_group: Easter # Group that gets the discount, leave blank for no group
  locked: false # Set true if you dont want regular players to access.
  locked_bypass_permission: bypass.perm # Set to '' or set if you want a custom bypass perm
  style: CUSTOM # 'AUTO' will have the plugin's default menu with sorting and all.
  # set to 'CUSTOM' to allow you to customize the menu
  size: 1 # Can only be used with 'CUSTOM'. Allows you to choose the gui rows. (1-6)
  filler: {} # This is a TBD thing for custom menus, ignore for now.
```
</details> 


## Shop Item Template

<details> 

<summary>Shop Item Template</summary>


```yml
'1':
  item_id: mortalitys_head # Must be unique
  shop_id: id # The shop this item is associated with. If empty, this will not be displayed anywhere.
  display_name: '&e&lMortality_'
  use_display_itemstack: false # If true, will use the display itemstack instead of the basic ones.
  display_itemstack: '' # Set in game
  display_material: minecraft:player_head # The material it will display as otherwise, will use default
  description: # Optional, gives more detail to the item
  - '&f - 500,000 Blocks Mined'
  price: 12000 # Set to -1 for shop default
  discount: 0 # Discount for item (0-100)
  allow_discount_stacking: false # True to allow  
  group: Easter # Optional but must have consistent name to with others to work.
  purchased_command: /home %player% # Runs this command when purchased.
  purchased_permission: '*' # The permission they receieve when purchased.
  purchased_item: '' # Set in game. The item they receive when purchased.
  permission: owned.permission # Optional but will work with "owned/unowned" filter for players.
  one_time_purchase: true # Will only allow a player to purchase this once.
  visible: true # Must have a purchased_item, purchased_command or purchased_permission to allow 'true'. Otherwise defaults to false.
  locked: false # Disable purchasing of item.
  locked_reason: '&cItem cannot be purchased.'
  archived: false # Ignore, for future use.
  shop_slot: 0 # Slot in CUSTOM menu.
```
</details> 


## To Do
- [X] v1.0
    - [X] Release 
- [ ] v1.1
    - [ ] TBD