package com.gamingmesh.jobs.commands.list;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.CMILib.ItemReflection;
import com.gamingmesh.jobs.CMILib.RawMessage;
import com.gamingmesh.jobs.CMILib.ItemManager.CMIEntityType;
import com.gamingmesh.jobs.CMILib.ItemManager.CMIMaterial;
import com.gamingmesh.jobs.CMILib.VersionChecker.Version;
import com.gamingmesh.jobs.commands.Cmd;
import com.gamingmesh.jobs.commands.JobCommand;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobInfo;
import com.gamingmesh.jobs.container.Quest;
import com.gamingmesh.jobs.container.QuestObjective;
import com.gamingmesh.jobs.stuff.ChatColor;
import com.gamingmesh.jobs.stuff.PageInfo;
import com.gamingmesh.jobs.stuff.Util;

public class editquests implements Cmd {

    @SuppressWarnings("deprecation")
	@Override
    @JobCommand(721)
    public boolean perform(Jobs plugin, CommandSender sender, String[] args) {
	if (sender instanceof Player) {
		Player player = (Player) sender;

		if (args.length == 0) {
		    args = new String[] { "list" };
		}

		switch (args[0]) {
		case "list":
		    if (args.length == 1) {
			showPath(player, null, null, null);

			for (Job one : Jobs.getJobs()) {
			    RawMessage rm = new RawMessage();
			    rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.jobs", "%jobname%", one.getChatColor()
					+ one.getName()), one.getName(), "jobs editquests list " + one.getName());
			    rm.show(sender);
			}

			Util.getQuestsEditorMap().remove(player.getUniqueId());
			return true;
		    }

		    if (args.length == 2) {
			Job job = Jobs.getJob(args[1]);
			if (job == null)
			    return false;

			showPath(player, job, null, null);

			List<Quest> quests = job.getQuests();
			if (quests == null || quests.isEmpty()) {
			    return false;
			}

			for (Quest one : job.getQuests()) {
			    RawMessage rm = new RawMessage();
			    rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.quests", "%questname%", one.getQuestName()),
					one.getQuestName(), "jobs editquests list " + job.getName() + " " + one.getQuestName()
				+ " 1");
			    rm.show(sender);
			}

			Util.getQuestsEditorMap().remove(player.getUniqueId());
			return true;
		    }

		    if (args.length == 4) {
			Integer page = null;
			try {
			    page = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
			}

			if (page != null) {
			    Job job = Jobs.getJob(args[1]);
			    if (job == null)
				return false;

			    Quest quest = null;
			    for (Quest one : job.getQuests()) {
				if (one.getQuestName().equalsIgnoreCase(args[2])) {
				    quest = one;
				    break;
				}
			    }

			    if (quest == null) {
				return true;
			    }

			    HashMap<String, QuestObjective> obj = quest.getObjectives();
			    if (obj == null || obj.isEmpty())
				return false;

			    obj.entrySet().forEach(one -> showPath(player, job,
					obj.get(one.getValue().getTargetName()).getAction(), null));

			    QuestObjective o = null;
			    PageInfo pi = new PageInfo(15, obj.size(), page);

			    for (Entry<String, QuestObjective> one : obj.entrySet()) {
				if (!pi.isEntryOk())
				    continue;

				o = one.getValue();
				String target = o.getTargetName();

				String objName = target.toLowerCase().replace('_', ' ');
				objName = Character.toUpperCase(objName.charAt(0)) + objName.substring(1);
				objName = Jobs.getNameTranslatorManager().Translate(objName, o.getAction(), o.getTargetId(),
				    o.getTargetMeta(), target);
				objName = org.bukkit.ChatColor.translateAlternateColorCodes('&', objName);

				RawMessage rm = new RawMessage();
				rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.objectives", "%objectivename%", objName),
				    target, "jobs editquests list " + job.getName() + " " + quest.getQuestName() + " " + target);
				rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.objectiveRemove"),
				    "&cRemove", "jobs editquests remove " + job.getName() + " " + quest.getQuestName() + " " + target);
				rm.show(sender);
			    }

			    RawMessage rm = new RawMessage();
			    rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.objectiveAdd"),
					"&eAdd new", "jobs editquests add " + job.getName() + " " + quest.getQuestName()
					+ " " + o.getAction().getName());
			    rm.show(sender);

			    Util.getQuestsEditorMap().remove(player.getUniqueId());

			    Jobs.getInstance().ShowPagination(sender, pi.getTotalPages(), page,
					"jobs editquests list " + job.getName() + " " + quest.getQuestName() + " " + 0);
			    return true;
			}
		    }
		    break;
		case "remove":
		    if (args.length == 4) {
			Job job = Jobs.getJob(args[1]);
			if (job == null)
			    return false;

			List<Quest> quests = job.getQuests();
			if (quests == null || quests.isEmpty())
			    return false;

			Quest q = null;
			for (Quest n : quests) {
			    if (n.getQuestName().equalsIgnoreCase(args[2])) {
				q = n;
				break;
			    }
			}

			if (q == null) {
			    return true;
			}

			quests.remove(q);

			Jobs.getConfigManager().changeJobsSettings(q.getCurrentPath(), null);

			player.performCommand("jobs editquests list " + job.getName() + " " + q.getQuestName() + " 1");

			Util.getQuestsEditorMap().remove(player.getUniqueId());

			return true;
		    }
		    break;
		case "add":
		    if (args.length >= 4 && args.length <= 5) {
			Job job = Jobs.getJob(args[1]);
			if (job == null)
			    return false;

			Quest q = null;
			for (Quest n : job.getQuests()) {
			    if (n.getQuestName().equalsIgnoreCase(args[2])) {
				q = n;
				break;
			    }
			}

			if (q == null) {
			    return true;
			}

			ActionType actionT = ActionType.getByName(args[3]);
			if (actionT == null)
			    return false;

			int amount = 0;
			if (args.length == 5) {
			    try {
				amount = Integer.parseInt(args[4]);
			    } catch (NumberFormatException e) {
			    }
			}

			if (amount < 1) {
			    amount = 3;
			}

			RawMessage rm = new RawMessage();
			rm.add(Jobs.getLanguage().getMessage("command.editquests.help.modify.enter"));
			rm.add(Jobs.getLanguage().getMessage("command.editquests.help.modify.hand"),
			    Jobs.getLanguage().getMessage("command.editquests.help.modify.handHover"), "jobs editquests add " + job.getName()
			    + " " + q.getQuestName() + " " + actionT.getName() + " hand " + amount);
			rm.add(Jobs.getLanguage().getMessage("command.editquests.help.modify.or"));
			rm.add(Jobs.getLanguage().getMessage("command.editquests.help.modify.look"),
			    Jobs.getLanguage().getMessage("command.editquests.help.modify.lookHover"), "jobs editquests add " + job.getName()
			    + " " + q.getQuestName() + " " + actionT.getName() + " looking " + amount);
			rm.show(sender);

			Util.getQuestsEditorMap().put(player.getUniqueId(), "jobs editquests add " + job.getName() +
			    " " + q.getQuestName() + " " + actionT.getName() + " " + amount);
			return true;
		    }

		    if (args.length >= 5 && args.length <= 6) {
			Job job = Jobs.getJob(args[1]);
			if (job == null)
			    return false;

			Quest q = null;
			for (Quest n : job.getQuests()) {
			    if (n.getQuestName().equalsIgnoreCase(args[2])) {
				q = n;
				break;
			    }
			}

			if (q == null) {
			    return false;
			}

			ActionType actionT = ActionType.getByName(args[3]);
			if (actionT == null) {
			    return false;
			}

			String key = args[4];
			switch (args[4]) {
			case "hand":
			    ItemStack item = Jobs.getNms().getItemInMainHand(player);
			    key = item.getType().name() + "-" + item.getData().getData();
			    break;
			case "offhand":
			    item = ItemReflection.getItemInOffHand(player);
			    key = item.getType().name() + "-" + item.getData().getData();
			    break;
			case "looking":
			case "lookingat":
			    Block block = Util.getTargetBlock(player, 30);
			    key = block.getType().name() + "-" + block.getData();
			    break;
			default:
			    break;
			}

			String myKey = key;
			String type = null;
			String subType = "";
			String meta = "";
			int id = 0;

			if (myKey.contains("-")) {
			    // uses subType
			    subType = ":" + myKey.split("-")[1];
			    meta = myKey.split("-")[1];
			    myKey = myKey.split("-")[0];
			}

			CMIMaterial material = null;

			switch (actionT) {
			case KILL:
			case MILK:
			case MMKILL:
			case BREED:
			case SHEAR:
			case EXPLORE:
			case CUSTOMKILL:
			    break;
			case TNTBREAK:
			case VTRADE:
			case SMELT:
			case REPAIR:
			case PLACE:
			case EAT:
			case FISH:
			case ENCHANT:
			case DYE:
			case CRAFT:
			case BREW:
			case BREAK:
			case STRIPLOGS:
			    material = CMIMaterial.get(myKey + (subType));

			    if (material == null)
				material = CMIMaterial.get(myKey.replace(" ", "_").toUpperCase());

			    if (material == null) {
				// try integer method
				Integer matId = null;
				try {
				    matId = Integer.valueOf(myKey);
				} catch (NumberFormatException e) {
				}
				if (matId != null) {
				    material = CMIMaterial.get(matId);
				    if (material != null) {
					Jobs.getPluginLogger().warning("Job " + job.getName() + " " + actionT.getName() + " is using ID: " + key + "!");
					Jobs.getPluginLogger().warning("Please use the Material name instead: " + material.toString() + "!");
				    }
				}
			    }
			    break;
			default:
			    break;

			}

			c: if (material != null && material.getMaterial() != null) {

			    // Need to include thos ones and count as regular blocks
			    switch (key.replace("_", "").toLowerCase()) {
			    case "itemframe":
				type = "ITEM_FRAME";
				meta = "1";
				break c;
			    case "painting":
				type = "PAINTING";
				meta = "1";
				break c;
			    case "armorstand":
				type = "ARMOR_STAND";
				meta = "1";
				break c;
			    default:
				break;
			    }

			    if (actionT == ActionType.BREAK || actionT == ActionType.PLACE || actionT == ActionType.STRIPLOGS) {
				if (!material.isBlock()) {
				    player.sendMessage(ChatColor.GOLD + "Quest " + q.getQuestName() + " has an invalid " + actionT.getName() + " type property: " + material
				+ "(" + key + ")! Material must be a block!");
				    break;
				}
			    }
			    if (material == CMIMaterial.REDSTONE_ORE && actionT == ActionType.BREAK && Version.isCurrentLower(Version.v1_13_R1)) {
				player.sendMessage(ChatColor.GOLD + "Quest " + q.getQuestName() + " is using REDSTONE_ORE instead of GLOWING_REDSTONE_ORE.");
				player.sendMessage(ChatColor.GOLD + "Automatically changing block to GLOWING_REDSTONE_ORE. Please update your configuration.");
				player.sendMessage(ChatColor.GOLD + "In vanilla minecraft, REDSTONE_ORE changes to GLOWING_REDSTONE_ORE when interacted with.");
				player.sendMessage(ChatColor.GOLD + "In the future, Jobs using REDSTONE_ORE instead of GLOWING_REDSTONE_ORE may fail to work correctly.");
				material = CMIMaterial.LEGACY_GLOWING_REDSTONE_ORE;
			    } else if (material == CMIMaterial.LEGACY_GLOWING_REDSTONE_ORE && actionT == ActionType.BREAK && Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
				player.sendMessage(ChatColor.GOLD + "Quest " + q.getQuestName() + " is using GLOWING_REDSTONE_ORE instead of REDSTONE_ORE.");
				player.sendMessage(ChatColor.GOLD + "Automatically changing block to REDSTONE_ORE. Please update your configuration.");
				material = CMIMaterial.REDSTONE_ORE;
			    }
			    id = material.getId();
			    type = material.toString();
			} else if (actionT == ActionType.KILL || actionT == ActionType.TAME || actionT == ActionType.BREED || actionT == ActionType.MILK) {

			    // check entities
			    EntityType entity = EntityType.fromName(myKey.toUpperCase());
			    if (entity == null) {
				entity = EntityType.valueOf(myKey.toUpperCase());
			    }

			    if (entity != null && entity.isAlive()) {
				type = entity.toString();
				id = entity.getTypeId();

				// using breeder finder
				if (actionT == ActionType.BREED)
				    Jobs.getGCManager().useBreederFinder = true;
			    }

			    if (entity == null) {
			    switch (key.toLowerCase()) {
			    case "skeletonwither":
				type = CMIEntityType.WITHER_SKELETON.name();
				id = 51;
				meta = "1";
				break;
			    case "skeletonstray":
				type = CMIEntityType.STRAY.name();
				id = 51;
				meta = "2";
				break;
			    case "zombievillager":
				type = CMIEntityType.ZOMBIE_VILLAGER.name();
				id = 54;
				meta = "1";
				break;
			    case "zombiehusk":
				type = CMIEntityType.HUSK.name();
				id = 54;
				meta = "2";
				break;
			    case "horseskeleton":
				type = CMIEntityType.SKELETON_HORSE.name();
				id = 100;
				meta = "1";
				break;
			    case "horsezombie":
				type = CMIEntityType.ZOMBIE_HORSE.name();
				id = 100;
				meta = "2";
				break;
			    case "guardianelder":
				type = CMIEntityType.ELDER_GUARDIAN.name();
				id = 68;
				meta = "1";
				break;
			    default:
				type = CMIEntityType.getByName(myKey.toUpperCase()).name();
				id = CMIEntityType.getByName(myKey.toUpperCase()).getId();
				meta = "1";
				break;
			    }
			    }

			} else if (actionT == ActionType.ENCHANT) {
			    Enchantment enchant = Enchantment.getByName(myKey);
			    if (enchant != null) {
				if (Jobs.getVersionCheckManager().getVersion().isEqualOrLower(Version.v1_12_R1)) {
				    try {
					id = (int) enchant.getClass().getMethod("getId").invoke(enchant);
				    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
				    }
				}
			    }
			    type = myKey;
			} else if (actionT == ActionType.CUSTOMKILL || actionT == ActionType.SHEAR || actionT == ActionType.MMKILL
				    || actionT == ActionType.COLLECT)
			    type = myKey;
			else if (actionT == ActionType.EXPLORE) {
			    type = myKey;
			    int a = 10;
			    try {
				a = Integer.valueOf(myKey);
			    } catch (NumberFormatException e) {
				player.sendMessage(ChatColor.GOLD + "Quest " + q.getQuestName() + " has an invalid " + actionT.getName() + " type property: " + key + "!");
				break;
			    }

			    Jobs.getExplore().setExploreEnabled();
			    Jobs.getExplore().setPlayerAmount(a + 1);
			} else if (actionT == ActionType.CRAFT && myKey.startsWith("!"))
			    type = myKey.substring(1, myKey.length());

			if (type == null) {
			    player.sendMessage(ChatColor.GOLD + "Quest " + q.getQuestName() + " has an invalid " + actionT.getName() + " type property: " + key + "!");
			    break;
			}

			if (actionT == ActionType.TNTBREAK)
			    Jobs.getGCManager().setTntFinder(true);

			int amount = 3;
			if (args.length == 6) {
			    try {
				amount = Integer.parseInt(args[5]);
			    } catch (NumberFormatException e) {
			    }
			}

			if (amount < 1) {
			    amount = 3;
			}

			QuestObjective questObj = new QuestObjective(actionT, id, meta, type + subType, amount);

			player.performCommand("jobs editquests list " + job.getName() + " " + actionT.getName() + " " + q.getQuestName());

			String path = q.getCurrentPath();
			path = path.replace("/", ".");

			org.bukkit.configuration.file.YamlConfiguration file = Jobs.getConfigManager().getJobConfig();
			for (String a : file.getConfigurationSection(path).getKeys(false)) {
			    if (a.equals("Target")) {
				Jobs.getConfigManager().changeJobsSettings(file.getString(a + ".Target"), (type + subType).toLowerCase());
				Jobs.getConfigManager().changeJobsSettings(file.getString(a + ".Action"), actionT.getName());
			    } else if (a.equals("Objectives")) {
				List<String> list = file.getStringList(a + ".Objectives");
				list.add(actionT.getName() + ";" + (type + subType).toLowerCase() + ";" + amount);

				Jobs.consoleMsg(org.apache.commons.lang.StringUtils.join(list, ", "));

				Jobs.getConfigManager().changeJobsSettings(a + ".Objectives", list);
			    }
			}

			Util.getQuestsEditorMap().remove(player.getUniqueId());

			return true;
		    }

		    break;
		default:
		    break;
		}
	}
	return false;
    }

    private static void showPath(Player player, Job job, ActionType action, JobInfo jInfo) {
	RawMessage rm = new RawMessage();
	rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.quest"), "&eQuest list", "jobs editquests");
	rm.show(player);

	if (job != null) {
	    rm = new RawMessage();
	    rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.jobs", "%jobname%", job.getChatColor()
			+ job.getName()), job.getName(), "jobs editquests list " + job.getName());
	    rm.show(player);
	}

	if (action != null && job != null) {
	    rm = new RawMessage();

	    rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.actions", "%actionname%", action.getName()),
			action.getName(), "jobs editquests list " + job.getName() + " " + action.getName()
		+ " 1");
	    rm.show(player);
	}

	if (action != null && job != null && jInfo != null) {
	    rm = new RawMessage();

	    String materialName = jInfo.getName().toLowerCase().replace('_', ' ');
	    materialName = Character.toUpperCase(materialName.charAt(0)) + materialName.substring(1);
	    materialName = Jobs.getNameTranslatorManager().Translate(materialName, jInfo);
	    materialName = org.bukkit.ChatColor.translateAlternateColorCodes('&', materialName);

	    rm.add(Jobs.getLanguage().getMessage("command.editquests.help.list.material", "%materialname%", jInfo.getName()),
			jInfo.getName(), "jobs editquests list " + job.getName() + " " + action.getName()
		+ " " + materialName);
	    rm.show(player);
	}
    }
}