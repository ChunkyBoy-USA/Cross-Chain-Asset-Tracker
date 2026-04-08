# Cross-Chain-Asset-Tracker
Cross-Chain Asset Tracker is a native Android Application to monitor rebase tokens and conduct cross-chain transfer.

![Cross-Chain Transfer](/transfer.gif "Cross-Chain Transfer")

![Real-time Balance & Interest Rate](/balance.gif "Real-time Balance & Interest Rate")

## Why I built this
This project demonstrates my solid Android experience combined with Smart Contract development (Solidity/Foundry).

## How To Build
Define variables in `local.properties`: `ARBITRUM_SEPOLIA_RPC_URL`, `ETHEREUM_SEPOLIA_RPC_URL`, [`REOWN_PROJECT_ID`](https://docs.reown.com/appkit/android/core/installation)

## Tech Stack
### Android (Native)
* Language: Kotlin

* UI: Jetpack Compose

* Architecture: MVVM (Data, Domain, UI)
* State Management: MVI
* Data: Coroutines & Flow, Hilt, Room, EncryptedSharedPreferences
* Network: Retrofit
* Web3: Web3j, WalletConnect

### Web3 (Smart Contracts)
* Smart Contracts: Solidity

* Framework: Foundry, Chainlink, Openzeppelin

* Features: Chainlink CCIP integration, Rebase Token, Token Pool, Vault Staking, Invariant testing.

## Main Features
1. Real-Time Balance and Interest Rate Updates. The app calculates rebase token balances on-device. It uses Kotlin Flow to show the balance changing in real-time without needing to refresh the screen.

2. Cross-Chain Transfer. Interact with smart contracts & the infrastructure to conduct cross-chain transfers. You can see when your tokens leave one chain and arrive on the next.

3. User Control and Authorization. Every transaction will be signed by Wallet without touching private keys.

4. Hardware-Level Security. Uses the Android Keystore to keep wallet addresses encrypted and safe.

## Smart Contract & Infrastructure (SSoT)
This repository serves as the single source of truth for the cross-chain logic. The Android app interacts directly with the following verified infrastructure: https://github.com/ChunkyBoy-USA/foundry-ccip-rebase-token

### Supported CCIP Lanes (Sepolia Testnets)
| Source Chain | Destination Chain | Pool Address | Rebase Token Address | Vault Address |
| :--- | :--- | :--- | :--- | :--- |
| **Ethereum** | Arbitrum | `0xf0c8726445ff84C22c785A970e54C6b00e655659` | `0x9c8276c5446574e12446eD893Ab5ae4561214979` | `0x9B309A8f1a314228eF225b21cAC3f27f3E0D3113`|
| **Arbitrum** | Ethereum | `0xa25048b542Ee3718b527c602b39b4D2C295D895F` | `0x03Eacf91aBF33470F22857B3C72Fe7e36aa87216` | `0xe3316131FB21FE2C07FdECECd172F9294E35D008`|


### Core Contract Logic
* **Rebase Token** Contracts released on **Arbitrum Sepolia** and **Ethereum Sepolia**.
* **Vault** Contracts for depositing ETH to mint Rebase Token on **Arbitrum Sepolia** and **Ethereum Sepolia**.
* **Pool** Contracts for burning or minting Rebase Token on **Arbitrum Sepolia** and **Ethereum Sepolia** respectively.
* **Verification:** All contracts are verified on **Arbiscan**, **Etherscan**, Explorer** using Foundry's `--verify` pipeline.

