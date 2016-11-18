package Vimda;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

import java.awt.*;

@ScriptManifest(category = Category.UTILITY, name = "Vimda", author = "DK", version = 1.0)
public class Vimda extends AbstractScript{

    private Area lumbyFish = new Area(3241, 3154, 3245, 3161, 0);
    private Area GE = new Area(3167, 3488, 3168, 3489, 0);
    private Area draynor = new Area(3092, 3240, 3097, 3246, 0);
    private Area lumby = new Area(3218, 3210, 3225, 3224, 0);
    private String state = "justStarted";

    @Override
    public int onLoop() {
        switch (state) {
            case "start":
                walkToLumbyFish();
                break;
            case "pickUpNet":
                pickUpNet();
                break;
            case "fish2Shrimps":
                fish2Shrimps();
                break;
            case "walkToGE":
                walkToGE();
                break;
            case "sellShrimpAndCancel":
                sellShrimpAndCancel();
                break;
            case "walkToDraynor":
                walkToDraynor();
                break;
            case "dieByWizard":
                dieByWizard();
                break;
            case "walkToGE2":
                walkToGE2();
                break;
            case "justStarted":
                justStarted();
                break;
            case "logOut":
                logOut();
                break;
        }
        return 600;
    }

    private void walkToLumbyFish() {
        log("walkToLumbyFish");
        if (!lumbyFish.contains(getLocalPlayer())) {
            if (getWalking().walk(lumbyFish.getRandomTile())) {
                sleepUntil(() -> lumbyFish.contains(getLocalPlayer()), Calculations.random(1500,3500));
            }
        } else {
            state = "pickUpNet";
        }
    }

    private void pickUpNet() {
        log("pickUpNet");
        if (!getInventory().contains("Small fishing net")) {
            if (getGroundItems().closest(i -> i.getName().equals("Small fishing net")).interact("Take")) {
                sleepUntil(() -> getInventory().contains("Small fishing net"), 15000);
            }
        } else {
            state = "fish2Shrimps";
        }
    }

    private void fish2Shrimps() {
        log("fish2Shrimps");
        if (getInventory().count("Raw shrimps") < 2) {
            Tile badSpot = new Tile(3246, 3157);
            NPC fishingSpot = getNpcs().closest(i -> i != null && i.getName().equals("Fishing spot") && !badSpot.equals(i.getTile()));
            Tile fishingSpotLocation = fishingSpot.getTile();
            if (fishingSpot != null && fishingSpot.interact("Net")) {
                sleepUntil(() -> getInventory().count("Raw shrimps") == 2 || !fishingSpot.getTile().equals(fishingSpotLocation), 60000);
            }
        } else {
            state = "walkToGE";
        }
    }

    private void walkToGE() {
        log("walkToGE");
        if (!GE.contains(getLocalPlayer())) {
            if (getWalking().walk(GE.getRandomTile())) {
                sleepUntil(() -> GE.contains(getLocalPlayer()), Calculations.random(1500,3500));
            }
        } else {
            state = "sellShrimpAndCancel";
        }
    }

    private void sellShrimpAndCancel() {
        log("sellShrimpAndCancel");
        log(getInventory().get("Raw shrimps").isNoted() + "");
        if (!getInventory().get("Raw shrimps").isNoted()) {
            NPC clerk = getNpcs().closest(n -> n != null & n.getName().equals("Grand Exchange Clerk"));
            if (clerk.interact("Exchange")) {
                if (sleepUntil(() -> getGrandExchange().isOpen(), 10000)) {
                    if (getGrandExchange().sellItem("Raw shrimps", 2, 57876)) {
                        if (getGrandExchange().confirm()) {
                            sleep(Calculations.random(3000,7000));
                            if (getGrandExchange().cancelOffer(0)) {
                                sleep(Calculations.random(2000,3000));
                                if (getWidgets().getWidget(465).getChild(23).getChild(2).interact("Collect-notes")) {
                                    if (sleepUntil(() -> getInventory().contains("Raw shrimps"), 8000)) {
                                        if (getGrandExchange().close()) {
                                            sleepUntil(() -> !getGrandExchange().isOpen(), 8000);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            state = "walkToDraynor";
        }
    }

    private void walkToDraynor() {
        log("walkToDraynor");
        if (!draynor.contains(getLocalPlayer())) {
            if (getWalking().walk(draynor.getRandomTile())) {
                sleepUntil(() -> draynor.contains(getLocalPlayer()), Calculations.random(1500,3500));
            }
        } else {
            state = "dieByWizard";
        }
    }

    private void dieByWizard() {
        log("dieByWizard");
        if (!lumby.contains(getLocalPlayer())) {
            NPC wizard = getNpcs().closest(npc -> npc != null && npc.getName().equals("Dark wizard") && npc.canAttack());
            if (wizard.interact("Attack")) {
                sleepUntil(() -> getSkills().getBoostedLevels(Skill.HITPOINTS) == 0, 60000);
            }
        } else {
            state = "walkToGE2";
        }
    }

    private void walkToGE2() {
        log("walkToGE2");
        if (!GE.contains(getLocalPlayer())) {
            if (getWalking().walk(GE.getRandomTile())) {
                sleepUntil(() -> GE.contains(getLocalPlayer()), Calculations.random(1500,3500));
            }
        } else {
            state = "justStarted";
        }
    }

    private void justStarted() {
        log("justStarted");
        getKeyboard().type("just started need advice");
        sleep(17000);
        state = "logOut";
    }

    private void logOut() {
        log("logout");
        getTabs().logout();
        stop();
    }
}