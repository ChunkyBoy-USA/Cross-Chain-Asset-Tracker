# Cross-Chain-Asset-Tracker
Cross-Chain Asset Tracker is a native Android Application to monitor rebase tokens and conduct cross-chain transactions. This project demonstrates my 9+ years of Android experience combined with Smart Contract development (Solidity/Foundry).

## Why I built this
Most mobile wallets do not show "Rebase" token balances correctly in real-time. Also, tracking cross-chain transfers (like Chainlink CCIP) is difficult on a phone. I built this to provide a clean, technical solution for mobile users.

## Tech Stack
### Android (Native)
* Language: Kotlin

* UI: Jetpack Compose

* Architecture: Clean Architecture + MVVM

* Data: Coroutines & Flow (Real-time updates), Hilt (DI), Room (Local Database)

* Web3: Web3j / WalletConnect v2

### Web3 (Smart Contracts)
* Smart Contracts: Solidity

* Framework: Foundry (Forge & Cast)

* Features: Chainlink CCIP integration, Rebase logic, Invariant testing.

## Main Features
1. Real-Time Balance Updates
The app calculates rebase token balances on-device. It uses Kotlin Flow to show the balance increasing in real-time without needing to refresh the screen.

2. CCIP Status Tracking
Uses the Chainlink CCIP API to show a step-by-step progress bar for cross-chain transfers. You can see when your tokens leave one chain and arrive on the next.

3. Node Fallback System
If one RPC node (like Infura) is slow, the app automatically switches to another node (like Alchemy). This keeps the app fast and reliable.

4. Hardware-Level Security
Uses the Android Keystore to keep your wallet addresses and session data encrypted and safe.

## Smart Contract & Infrastructure (SSoT)
This repository serves as the single source of truth for the cross-chain logic. The Android app interacts directly with the following verified infrastructure: https://github.com/ChunkyBoy-USA/foundry-ccip-rebase-token

### Supported CCIP Lanes (Sepolia Testnets)
| Source Chain | Destination Chain | Pool Address | Rebase Token Address |
| :--- | :--- | :--- | :--- |
| **Ethereum** | Arbitrum | `0xf0c8726445ff84C22c785A970e54C6b00e655659` | `0x9c8276c5446574e12446eD893Ab5ae4561214979`
| **Arbitrum** | Ethereum | `0xa25048b542Ee3718b527c602b39b4D2C295D895F` | `0x03Eacf91aBF33470F22857B3C72Fe7e36aa87216`


### Core Contract Logic
* **Rebase Token** Contracts released on **Arbitrum Sepolia** and **Ethereum Sepolia**.
* **Vault** Contract on Ethereum Sepolia for depositing ETH to mint Rebase Token.
* **Verification:** All contracts are verified on **Arbiscan**, **Etherscan**, Explorer** using Foundry's `--verify` pipeline.

