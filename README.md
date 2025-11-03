# 🏹 SWC-Bounties

A lightweight bounty system for Minecraft — place, track, and claim bounties with ease.

---

## ⚙️ Commands

```yaml
/bounty help                      # 📘 Show the help menu
/bounty place <target> <amount> <expire>   # 💰 Place a bounty on a player
/bounty remove <target>           # ❌ Remove an active bounty
/bounty track <target>            # 🎯 Track a target
/bounty random                    # 🎲 Place a random bounty (Admin only)
/bounty board                     # 🪧 View the bounty board
/bounty claim                     # 🏆 Claim a bounty (Admin only)
/bounty info                      # ℹ️ View detailed bounty info
```

---

## 🔒 Permissions

```ini
swcb.bounty.place          ; Allows placing bounties
swcb.admin.bounty.remove   ; Allows removing bounties
swcb.bounty.track          ; Allows tracking bounty targets
swcb.bounty.board          ; Allows viewing the bounty board
swcb.admin.bounty.claim    ; Allows claiming bounties
```

---

## 🧱 Example Usage

```bash
# Player places a bounty
/bounty place Notch 5000 2d

# Admin checks bounty board
/bounty board

# Admin claims a bounty reward
/bounty claim
```

---

💀 **Hunt smart. Claim big. Survive the chase.** 💰
