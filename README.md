# Locks 1.20.1 (多平台锁模组 / Cross-platform Lock Mod)

基于 Architectury 框架开发的 Minecraft 1.20.1 多平台（Forge/Fabric）锁与安全机制模组。
A Minecraft 1.20.1 cross-platform (Forge/Fabric) lock and security mod built with the Architectury API.

---

## 📥 简介 / Introduction
**Locks** 是一个专注于在 Minecraft 中实现物理容器锁以及相关玩法的模组。它不仅能够保护你的私有财产，还能与世界生成的遗迹进行深度结合，为你探索地牢、村庄等结构时带来更具沉浸感的开锁与寻宝体验。

**Locks** is a mod focused on implementing physical container locks and related gameplay mechanics in Minecraft. Not only does it protect your private property, but it also deeply integrates with world-generated structures, bringing a more immersive lock-picking and treasure-hunting experience when you explore dungeons, villages, and other ruins.

## ✨ 核心特性 / Features

*   **多层级锁具 / Multi-tier Locks**: 提供木锁、铁锁、钢锁、金锁、钻石锁、下界合金锁等。锁具的品质将直接影响其开锁难度与抗破坏能力。  
    Provides Wood, Iron, Steel, Gold, Diamond, and Netherite locks. The quality of the lock directly affects its lock-picking difficulty and resistance to being broken.
    
*   **钥匙系统与钥匙环 / Keys & Key Rings**: 自由复制、配对你的钥匙。使用**钥匙环**将钥匙串联管理，或是使用更高阶的**万能钥匙**。  
    Freely copy and pair your keys. Use a **Key Ring** to manage your keys together, or use the high-tier **Master Key**.
    
*   **撬锁小游戏 / Lock-picking Minigame**: 忘带钥匙？使用**开锁器**体验自定义撬锁机制（带有动态动画效果与音效反馈），不同材质锁芯的挑战难度截然不同。  
    Forgot your key? Use a **Lock Pick** to experience the custom lock-picking mechanic (with dynamic animations and sound feedback). Different lock materials offer completely different levels of challenge.
    
*   **暴力破锁 / Brute-force Lock Breaking**: 允许使用斧头等高伤害工具强行破锁！系统将动态计算工具材质效率、附魔（如耐久减免）等属性。强行破坏某些高级锁具甚至会触发特定的反弹伤害（如电击惩罚）。  
    Allows the use of high-damage tools like axes to forcefully break locks! The system dynamically calculates tool material efficiency and enchantments (like Unbreaking mitigating durability loss). Forcefully breaking certain high-level locks may even trigger specific recoil damage (like shock penalties!).
    
*   **世界生成与战利品池整合 / World Gen & Loot Table Integration**: 完美融入原版世界。在世界生成的战利品宝箱（如废弃矿井、下界要塞、村庄铁匠铺等）上自动随机生成符合群系难度的物理锁。  
    Perfectly integrates into the vanilla world. Physical locks that match the structure's difficulty are automatically and randomly generated on world-generated loot chests (such as abandoned mineshafts, nether fortresses, village toolsmiths, etc.).
    
*   **跨平台兼容 / Cross-platform Compatibility**: 采用 Architectury 架构原生构建，共享核心逻辑的同时无缝支持 Fabric 与 Forge API。  
    Natively built with the Architectury framework, sharing core logic while seamlessly supporting both Fabric and Forge APIs.

## 🛠️ 构建指南 / Build Instructions

本项目采用 Gradle 进行构建。开发环境已配置完整的多模块系统。
This project uses Gradle for building. The development environment is configured with a complete multi-module system.

### 前置要求 / Prerequisites
*   Java 17
*   Minecraft 1.20.1

### 编译与打包 / Compiling & Building
在项目根目录下打开终端，运行：
Open a terminal in the project root directory and run:

```bash
# Windows
gradlew build

# Linux / macOS
./gradlew build
```

编译成功后，可以直接到对应平台目录获取 Mod 文件：
After a successful build, you can grab the compiled Mod files directly from the corresponding platform directories:
*   **Forge**: `forge/build/libs/`
*   **Fabric**: `fabric/build/libs/`

## ⚙️ 配置系统 / Configuration

配置分为两部分 / Configuration is divided into two parts:
1. **配置文件 (Config)**：位于 `config/` 目录下。例如 `locks-server.toml` 可精细控制工具破锁耗损、生成概率、附魔惩罚等机制。(修改配置后会同步应用)  
   Located in the `config/` directory. For example, `locks-server.toml` allows detailed control over tool durability loss when breaking locks, generation probabilities, enchantment penalties, etc.
   
2. **数据包 (Data Pack)**：锁的生成规则、权重以及白名单等已高度数据化。修改或覆盖 `data/locks/loot_tables/` 下的表单能够自由调整自然生成的结构锁。  
   The generation rules, weights, and whitelists for locks are highly data-driven. Modifying or overriding the tables under `data/locks/loot_tables/` allows you to freely adjust naturally generated structure locks.

## 📝 开发者说明 / Developer Notes
在构建和维护本项目代码时，严格遵循 SOLID 设计原则以及模块化分离的要求。尽量保证单个类的单一职责，并在跨平台处理时多使用通用接口。  
When building and maintaining this project's code, strictly follow SOLID design principles and modular separation requirements. Try to ensure the single responsibility of a single class and use common interfaces frequently when handling cross-platform logic.
