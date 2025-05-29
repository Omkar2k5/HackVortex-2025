# Smart Finance Tracker

## HackVortex 2025 - FinTech Domain

## Problem Statement

In today's fast-paced world, individuals often struggle to effectively manage and track their financial transactions. Bank SMS alerts, while informative, can be overwhelming and difficult to organize. Users find it challenging to get a holistic view of their financial health, including credited and debited amounts, transaction dates, and sources/destinations of funds, all in one accessible location. This lack of a centralized and organized system leads to poor budgeting habits, difficulty in identifying spending patterns, and a general lack of financial awareness.

## Approach & Solution

Smart Finance Tracker aims to revolutionize personal finance management by leveraging the power of mobile technology and web platforms. Our solution tackles the problem by:

1.  **Automated SMS Parsing:** The Android application intelligently parses bank transaction SMS messages, extracting crucial details such as credited/debited amounts, transaction dates, and sender/receiver information.
2.  **Centralized Database:** This extracted data is then securely stored in a Firebase database, providing a centralized repository for all financial transactions.
3.  **Intuitive Web Dashboard:** A Next.js web application serves as the primary interface for users to visualize their financial data. This dashboard offers a comprehensive overview of:
    - **Transaction History:** A detailed, filterable list of all credited and debited transactions.
    - **Budget Management:** Tools to set and track budgets across various categories.
    - **Income & Expense Tracking:** Clear visualizations of income sources and spending patterns.
    - **Categorization:** Automated or manual categorization of transactions for better insights.
4.  **User-Friendly Interface:** Both the mobile and web applications are designed with user experience in mind, offering a clean, intuitive, and easy-to-navigate interface.

## Features

- **Automated Transaction Logging:** Parses bank SMS messages to automatically log transactions.
- **Comprehensive Transaction History:** View all credited, debited, and categorized transactions in one place.
- **Budgeting Tools:** Create and manage budgets for different spending categories.
- **Income & Expense Tracking:** Visualize income sources and expenditure patterns with interactive charts.
- **Categorization of Transactions:** Automatically or manually categorize transactions for detailed financial insights.
- **Search & Filter Functionality:** Easily search and filter transactions by date, amount, category, or sender/receiver.
- **Cross-Platform Accessibility:** Access your financial data seamlessly across Android and web platforms.
- **Secure Data Storage:** Your financial data is securely stored and managed with Firebase.
- **APK Download:** Users can easily download the Android application (APK) directly from the website for convenient installation.

## Tech Stack

- **Frontend (Web):** Next.js
- **Backend & Database:** Firebase
- **Mobile (Android):** Kotlin

<!-- ## Screenshots

*(Please include actual screenshots of your application here. You can add images like:)*

  * **Home Dashboard View:**
      * [Image of the Smart Finance Tracker web dashboard showing an overview of transactions, budget, and income/expenses.]
  * **Transaction List View:**
      * [Image of the web application showing a detailed list of transactions with filters.]
  * **Budgeting Section:**
      * [Image of the web application displaying the budget management interface.]
  * **Android App (SMS Parsing/Initial Setup):**
      * [Image of the Android app's interface, possibly showing SMS parsing in action or the initial setup screen.]

 -->

## Run Instructions

To set up and run the Smart Finance Tracker locally, please follow these steps:

### Prerequisites

- Node.js (LTS version recommended)
- npm or yarn
- Android Studio (for running the Kotlin app)
- Firebase Project (with a configured database)

### 1\. Firebase Setup

1.  Create a new Firebase project at [console.firebase.google.com](https://console.firebase.google.com/).
2.  Set up Firestore Database in Native mode.
3.  Enable Google Authentication (or any other authentication method you plan to use).
4.  Download your Firebase configuration files:
    - For Web (Next.js): `firebaseConfig.js` or similar.
    - For Android (Kotlin): `google-services.json` (place it in the `android/app` directory).

### 2\. Frontend (Next.js)

1.  Navigate to the `frontend` directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    # or
    yarn install
    ```
3.  Create a `.env.local` file in the `frontend` directory and add your Firebase configuration:
    ```
    NEXT_PUBLIC_FIREBASE_API_KEY=your_api_key
    NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=your_auth_domain
    NEXT_PUBLIC_FIREBASE_PROJECT_ID=your_project_id
    NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=your_storage_bucket
    NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=your_messaging_sender_id
    NEXT_PUBLIC_FIREBASE_APP_ID=your_app_id
    NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID=your_measurement_id
    ```
4.  Run the development server:
    ```bash
    npm run dev
    # or
    yarn dev
    ```
5.  Open [http://localhost:3000](https://www.google.com/search?q=http://localhost:3000) in your browser to view the application.

### 3\. Android (Kotlin)

1.  Open the `android` directory in Android Studio.
2.  Ensure you have the `google-services.json` file placed in the `android/app` directory.
3.  Sync your project with Gradle files (File -\> Sync Project with Gradle Files).
4.  Run the app on an Android emulator or a physical device:
    - Select your desired device/emulator from the toolbar.
    - Click the "Run 'app'" button (green triangle).

**Note:** For the SMS parsing functionality to work on an Android device, ensure the app has the necessary SMS read permissions granted. You might need to manually grant these permissions in the device settings if not prompted automatically.
