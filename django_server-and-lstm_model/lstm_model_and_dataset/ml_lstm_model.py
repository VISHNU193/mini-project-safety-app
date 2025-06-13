import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.metrics import (classification_report, confusion_matrix, roc_auc_score,
                             roc_curve, precision_recall_curve, make_scorer,
                             recall_score, f1_score)
import seaborn as sns
import os
import joblib # For saving/loading sklearn objects

# TensorFlow / Keras
from tensorflow.keras.models import Sequential # Removed: load_model as load_keras_model (re-added if needed later)
from tensorflow.keras.layers import LSTM, Dense, Dropout, Input
from tensorflow.keras.callbacks import EarlyStopping
from sklearn.utils import class_weight # For Keras class weights
# If you need to load a Keras model later, uncomment and use:
# from tensorflow.keras.models import load_model as load_keras_model


# Imbalanced-learn for SMOTE
try:
    from imblearn.over_sampling import SMOTE
    IMBLEARN_AVAILABLE = True
except ImportError:
    IMBLEARN_AVAILABLE = False
    print("Warning: imbalanced-learn (for SMOTE) is not installed. Run '!pip install imbalanced-learn'. SMOTE will be skipped.")

# XGBoost (Optional, for an alternative fall detection model)
try:
    import xgboost as xgb
    XGBOOST_AVAILABLE = True
except ImportError:
    XGBOOST_AVAILABLE = False
    print("Warning: xgboost is not installed. Run '!pip install xgboost'. XGBoost model will be skipped.")


# --- Configuration ---


# OPTION 2: Files in Google Drive (Mount Drive First)
# Make sure to uncomment these lines if using Google Drive and adjust the path
# from google.colab import drive
# drive.mount('/content/drive', force_remount=True) # force_remount can be useful

# Local file paths - adjust these to your actual data file locations
BASE_DATA_DIR = os.path.dirname(os.path.abspath(__file__))  # Current directory
VITALS_DATASET_PATH = os.path.join(BASE_DATA_DIR, 'human_vital_signs_dataset_2024.csv')
FALL_DATASET_PATH = os.path.join(BASE_DATA_DIR, 'acc_gyr.csv')


# --- Vitals LSTM Configuration ---
TIME_STEPS_VITALS = 10
N_FEATURES_VITALS = 3   # Heart Rate, Oxygen Saturation, Body Temperature
N_CLASSES_VITALS = 4    # Default: High, Medium, Low, Normal. Adjust if create_risk_labels_vitals changes this.

# --- Fall Detection Configuration ---
FALL_FEATURE_COLS_RAW = ['xAcc', 'yAcc', 'zAcc', 'xGyro', 'yGyro', 'zGyro']
FALL_LABEL_COL = 'label' # <<<--- CRITICAL FIX: Changed from 'FallLabel'
WINDOW_SIZE_SECONDS = 1.5
SAMPLING_RATE_HZ = 50 # <<<--- ADJUST THIS TO YOUR acc_gyr.csv SAMPLING RATE
WINDOW_SAMPLES = int(WINDOW_SIZE_SECONDS * SAMPLING_RATE_HZ)
STEP_SAMPLES = WINDOW_SAMPLES // 2 # 50% overlap

SAMPLE_DATA_PLOT_RANGE = 300 # For visualization

# --- Helper Function for Plotting ROC Curve ---
def plot_roc_curve_custom(y_true, y_pred_proba, model_name="Model", ax=None):
    fpr, tpr, _ = roc_curve(y_true, y_pred_proba)
    auc = roc_auc_score(y_true, y_pred_proba)
    if ax is None:
        fig, ax_roc_plot = plt.subplots(figsize=(8, 6)) # Use a different variable name
    else:
        ax_roc_plot = ax
    ax_roc_plot.plot(fpr, tpr, label=f'{model_name} (AUC = {auc:.3f})')
    ax_roc_plot.plot([0, 1], [0, 1], 'k--')
    ax_roc_plot.set_xlabel('False Positive Rate')
    ax_roc_plot.set_ylabel('True Positive Rate')
    ax_roc_plot.set_title(f'ROC Curve - {model_name}')
    ax_roc_plot.legend(loc='lower right')
    ax_roc_plot.grid(True)
    if ax is None: plt.show()


# --- Helper Function for Plotting Precision-Recall Curve ---
def plot_precision_recall_curve_custom(y_true, y_pred_proba, model_name="Model", ax=None):
    precision, recall, _ = precision_recall_curve(y_true, y_pred_proba)
    if ax is None:
        fig, ax_pr_plot = plt.subplots(figsize=(8, 6)) # Use a different variable name
    else:
        ax_pr_plot = ax
    ax_pr_plot.plot(recall, precision, label=model_name)
    ax_pr_plot.set_xlabel('Recall')
    ax_pr_plot.set_ylabel('Precision')
    ax_pr_plot.set_title(f'Precision-Recall Curve - {model_name}')
    ax_pr_plot.legend(loc='lower left')
    ax_pr_plot.grid(True)
    if ax is None: plt.show()

# ==============================================================================
# PART 1: VITAL SIGNS ANOMALY DETECTION (LSTM)
# ==============================================================================
print("========================================================")
print("PART 1: VITAL SIGNS ANOMALY DETECTION (LSTM)")
print("========================================================")

vitals_full_df = None
try:
    if not os.path.exists(VITALS_DATASET_PATH):
        raise FileNotFoundError(f"Vitals dataset file not found at '{VITALS_DATASET_PATH}'")
    vitals_full_df = pd.read_csv(VITALS_DATASET_PATH)
    print(f"Successfully loaded vitals dataset: {VITALS_DATASET_PATH}")
    print("Vitals dataset columns:", vitals_full_df.columns.tolist())

    # Display sample data and stats from REAL vitals data
    print("\nSample Vitals Data (first 5 rows from real data):")
    print(vitals_full_df[['Heart Rate', 'Oxygen Saturation', 'Body Temperature']].head())
    print("\nDescriptive Statistics for Vitals (from real data):")
    print(vitals_full_df[['Heart Rate', 'Oxygen Saturation', 'Body Temperature']].describe())

except FileNotFoundError as e:
    print(f"Error: {e}")
    print("Generating a dummy vitals dataset as real one not found...")
    n_rows = 2000
    vitals_full_df = pd.DataFrame({
        'Heart Rate': np.random.randint(50, 120, n_rows),
        'Oxygen Saturation': np.random.randint(90, 101, n_rows),
        'Body Temperature': np.random.normal(36.8, 0.5, n_rows).round(1),
    })
    vitals_full_df['Body Temperature'] = np.clip(vitals_full_df['Body Temperature'], 35.0, 41.0)
    print("Loaded dummy vitals dataset.")
except Exception as e:
    print(f"An error occurred while loading the vitals dataset: {e}")
    vitals_full_df = None # Ensure it's None to skip processing if error

if vitals_full_df is not None:
    vitals_columns_for_lstm = ['Heart Rate', 'Oxygen Saturation', 'Body Temperature']
    if not all(col in vitals_full_df.columns for col in vitals_columns_for_lstm):
        print(f"Error: One or more vital columns {vitals_columns_for_lstm} not found in the vitals dataset.")
        vitals_full_df = None # Skip processing

if vitals_full_df is not None:
    vitals_df_lstm = vitals_full_df[vitals_columns_for_lstm].copy()

    def create_risk_labels_vitals(df_in):
        df = df_in.copy()
        # --- YOU NEED TO ADJUST THESE THRESHOLDS BASED ON YOUR REAL DATA DISTRIBUTION ---
        # Example:
        # High: Very abnormal HR OR very low SpO2
        # Medium: Moderately abnormal HR OR moderately low SpO2
        # Low: Abnormal Body Temp (if others are okay)
        conditions = [
            (df['Heart Rate'] > 100) | (df['Heart Rate'] < 50) | (df['Oxygen Saturation'] < 90), # High risk
            (df['Heart Rate'] > 90)  | (df['Heart Rate'] < 60) | (df['Oxygen Saturation'] < 94), # Medium risk
            (df['Body Temperature'] > 37.5) | (df['Body Temperature'] < 36.0)                  # Low risk
        ]
        risk_values = ['High', 'Medium', 'Low']
        df['Risk'] = np.select(conditions, risk_values, default='Normal')
        return df['Risk']

    vitals_df_lstm['Risk'] = create_risk_labels_vitals(vitals_df_lstm)
    print("\nVitals Risk distribution (after create_risk_labels_vitals applied):")
    print(vitals_df_lstm['Risk'].value_counts(normalize=True))
    print(vitals_df_lstm['Risk'].value_counts())


    label_encoder_vitals = LabelEncoder()
    vitals_df_lstm['Risk_Encoded'] = label_encoder_vitals.fit_transform(vitals_df_lstm['Risk'])
    joblib.dump(label_encoder_vitals, 'vitals_risk_label_encoder.pkl')
    print("Vitals Risk Label Encoder classes:", label_encoder_vitals.classes_)

    # Update N_CLASSES_VITALS based on actual unique classes found
    N_CLASSES_VITALS_ACTUAL = len(label_encoder_vitals.classes_)
    if N_CLASSES_VITALS_ACTUAL != N_CLASSES_VITALS:
        print(f"Warning: N_CLASSES_VITALS was {N_CLASSES_VITALS}, but found {N_CLASSES_VITALS_ACTUAL} unique risk classes. Adjusting N_CLASSES_VITALS.")
        N_CLASSES_VITALS = N_CLASSES_VITALS_ACTUAL


    features_to_scale_vitals = vitals_df_lstm[vitals_columns_for_lstm].values
    scaler_vitals = StandardScaler()
    scaled_features_vitals = scaler_vitals.fit_transform(features_to_scale_vitals)
    joblib.dump(scaler_vitals, 'vitals_scaler.pkl')

    def create_sequences_lstm(input_data, target_data, time_steps):
        Xs, ys = [], []
        for i in range(len(input_data) - time_steps):
            Xs.append(input_data[i:(i + time_steps)])
            ys.append(target_data[i + time_steps -1])
        if not Xs: return np.array([]), np.array([])
        return np.array(Xs), np.array(ys)

    X_vitals, y_vitals = create_sequences_lstm(scaled_features_vitals, vitals_df_lstm['Risk_Encoded'].values, TIME_STEPS_VITALS)

    if X_vitals.shape[0] < 50 or N_CLASSES_VITALS < 2:
        print(f"Not enough data/classes for vitals model training (Sequences: {X_vitals.shape[0]}, Classes: {N_CLASSES_VITALS}). Skipping.")
    else:
        X_train_v, X_test_v, y_train_v, y_test_v = train_test_split(
            X_vitals, y_vitals, test_size=0.2, random_state=42,
            stratify=y_vitals if np.unique(y_vitals).size > 1 else None)

        keras_class_weights_dict_v = None
        if N_CLASSES_VITALS > 1 : # Only compute if multiple classes
            unique_classes_v_train, _ = np.unique(y_train_v, return_counts=True)
            if len(unique_classes_v_train) >= 2: # Need at least 2 classes for balanced weights
                try:
                    keras_class_weights_v = class_weight.compute_class_weight(
                        'balanced', classes=np.unique(y_train_v), y=y_train_v # Use unique classes from y_train_v
                    )
                    keras_class_weights_dict_v = dict(enumerate(keras_class_weights_v))
                    print("Keras Class Weights for Vitals:", keras_class_weights_dict_v)
                except ValueError as e_cw: # Handles cases where a class might be missing after split
                    print(f"Could not compute Keras class weights: {e_cw}. Using uniform weights.")
            else:
                print("Warning: Less than 2 classes in y_train_v. Using uniform weights for Keras.")
        else:
            print("Only one risk class identified. Using uniform weights for Keras.")


        model_vitals_lstm = Sequential([
            Input(shape=(TIME_STEPS_VITALS, N_FEATURES_VITALS)),
            LSTM(128, return_sequences=True, kernel_regularizer='l2'), # Added L2 regularization
            Dropout(0.3),
            LSTM(64, kernel_regularizer='l2'),
            Dropout(0.3),
            Dense(N_CLASSES_VITALS, activation='softmax') # N_CLASSES_VITALS is now dynamic
        ])
        model_vitals_lstm.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])
        model_vitals_lstm.summary()
        early_stopping_vitals = EarlyStopping(monitor='val_loss', patience=15, restore_best_weights=True, verbose=1)

        print("\nTraining LSTM model for vitals...")
        history_vitals = model_vitals_lstm.fit(
            X_train_v, y_train_v,
            epochs=100, batch_size=64, # Increased batch size
            validation_data=(X_test_v, y_test_v),
            callbacks=[early_stopping_vitals],
            class_weight=keras_class_weights_dict_v,
            verbose=1
        )
        model_vitals_lstm.save('vital_signs_lstm_model.keras')
        print("Trained Vitals LSTM model saved as vital_signs_lstm_model.keras")

        print("\nEvaluating Vitals LSTM model...")
        loss_v, acc_v = model_vitals_lstm.evaluate(X_test_v, y_test_v, verbose=0)
        print(f"Vitals LSTM Test Loss: {loss_v:.4f}, Test Accuracy: {acc_v:.4f}")

        pred_proba_v = model_vitals_lstm.predict(X_test_v)
        pred_encoded_v = np.argmax(pred_proba_v, axis=1)

        print("\nClassification Report (Vitals LSTM):")
        target_names_report_v = label_encoder_vitals.classes_
        labels_report_v = np.arange(len(target_names_report_v))
        print(classification_report(y_test_v, pred_encoded_v, labels=labels_report_v, target_names=target_names_report_v, zero_division=0, digits=4))

        cm_v = confusion_matrix(y_test_v, pred_encoded_v, labels=labels_report_v)
        plt.figure(figsize=(max(6, N_CLASSES_VITALS*2), max(4, N_CLASSES_VITALS*1.5)))
        sns.heatmap(cm_v, annot=True, fmt='d', cmap='Blues', xticklabels=target_names_report_v, yticklabels=target_names_report_v)
        plt.title('Confusion Matrix - Vitals LSTM'); plt.xlabel('Predicted'); plt.ylabel('True'); plt.show()

# ==============================================================================
# PART 2: ADVANCED FALL DETECTION (Accelerometer/Gyroscope Data)
# ==============================================================================
print("\n========================================================")
print("PART 2: ADVANCED FALL DETECTION (ACCELEROMETER/GYROSCOPE)")
print("========================================================")

# --- Feature Extraction Function ---
def extract_windowed_features(df_segment, window_size, step_size, acc_cols, gyro_cols, label_col=None):
    all_window_features = []
    num_samples = len(df_segment)
    if num_samples < window_size:
        print(f"Warning: Data segment length ({num_samples}) is less than window size ({window_size}). No windows extracted.")
        return pd.DataFrame(all_window_features)

    for i in range(0, num_samples - window_size + 1, step_size):
        window = df_segment.iloc[i : i + window_size]
        current_features = {}
        if label_col and label_col in window.columns:
            label_val = window[label_col].iloc[window_size // 2]
            current_features[label_col] = int(label_val)

        if all(c in window.columns for c in acc_cols):
            smv_acc = np.sqrt(window[acc_cols[0]]**2 + window[acc_cols[1]]**2 + window[acc_cols[2]]**2)
            current_features['SMV_Acc_mean'] = smv_acc.mean(); current_features['SMV_Acc_std'] = smv_acc.std()
            current_features['SMV_Acc_min'] = smv_acc.min(); current_features['SMV_Acc_max'] = smv_acc.max()
            current_features['SMV_Acc_median'] = smv_acc.median(); current_features['SMV_Acc_iqr'] = smv_acc.quantile(0.75) - smv_acc.quantile(0.25)
            for col_name in acc_cols:
                current_features[f'{col_name}_mean'] = window[col_name].mean(); current_features[f'{col_name}_std'] = window[col_name].std()
                current_features[f'{col_name}_max_abs_diff'] = window[col_name].diff().abs().max()

        if all(c in window.columns for c in gyro_cols):
            smv_gyro = np.sqrt(window[gyro_cols[0]]**2 + window[gyro_cols[1]]**2 + window[gyro_cols[2]]**2)
            current_features['SMV_Gyro_mean'] = smv_gyro.mean(); current_features['SMV_Gyro_std'] = smv_gyro.std()
            current_features['SMV_Gyro_max'] = smv_gyro.max(); current_features['SMV_Gyro_iqr'] = smv_gyro.quantile(0.75) - smv_gyro.quantile(0.25)
            for col_name in gyro_cols:
                current_features[f'{col_name}_mean'] = window[col_name].mean(); current_features[f'{col_name}_std'] = window[col_name].std()

        min_expected_features = 1 if label_col and label_col in current_features else 0
        if len(current_features) > min_expected_features: all_window_features.append(current_features)

    processed_df = pd.DataFrame(all_window_features)
    if not processed_df.empty:
         processed_df = processed_df.dropna(subset=[col for col in processed_df.columns if col != label_col], how='all')
    return processed_df

# --- Load Fall Dataset ---
print(f"\nAttempting to load fall dataset from: {FALL_DATASET_PATH}")
if not os.path.exists(FALL_DATASET_PATH):
    print(f"ERROR: File '{FALL_DATASET_PATH}' DOES NOT EXIST. Check path/upload file.")
    if '/content/drive/MyDrive/' in FALL_DATASET_PATH and not os.path.exists('/content/drive/MyDrive/'):
        print("Google Drive might not be mounted. Run the Drive mounting cell if using Drive paths.")
    elif os.path.exists('/content/'):
         print("Files in /content/:", os.listdir('/content/'))

fall_df_raw = None
try:
    if not os.path.exists(FALL_DATASET_PATH): raise FileNotFoundError(f"Fall data file missing: {FALL_DATASET_PATH}")
    fall_df_raw = pd.read_csv(FALL_DATASET_PATH)
    print(f"Successfully loaded fall dataset: {FALL_DATASET_PATH}")
    print("Fall dataset (raw) columns:", fall_df_raw.columns.tolist())
    if FALL_LABEL_COL not in fall_df_raw.columns: # FALL_LABEL_COL is now 'label'
        print(f"CRITICAL: RAW CSV does NOT contain the label column '{FALL_LABEL_COL}'. Checking for 'Activity' column...")
        if 'Activity' in fall_df_raw.columns:
            fall_df_raw[FALL_LABEL_COL] = fall_df_raw['Activity'].apply(lambda x: 1 if isinstance(x, str) and 'FALL' in x.upper() else 0)
            print(f"Derived '{FALL_LABEL_COL}' from 'Activity' column.")
        else: raise KeyError(f"'{FALL_LABEL_COL}' not found and no 'Activity' column to derive it from.")
    print("\nRAW FallLabel ('{}') distribution in loaded CSV:".format(FALL_LABEL_COL)); print(fall_df_raw[FALL_LABEL_COL].value_counts(normalize=True)); print(fall_df_raw[FALL_LABEL_COL].value_counts())
    if not all(col in fall_df_raw.columns for col in FALL_FEATURE_COLS_RAW):
        raise ValueError(f"Missing one or more raw feature columns for fall detection: {FALL_FEATURE_COLS_RAW}")
except (FileNotFoundError, pd.errors.EmptyDataError, KeyError, ValueError) as e_load:
    print(f"Error loading/processing fall dataset '{FALL_DATASET_PATH}': {e_load}")
    print("Generating a DUMMY fall dataset...")
    n_rows_fall = 10000
    fall_df_raw = pd.DataFrame(np.random.randn(n_rows_fall, len(FALL_FEATURE_COLS_RAW)) * np.array([3,3,3,80,80,80]), columns=FALL_FEATURE_COLS_RAW)
    fall_df_raw['zAcc'] -= 9.8
    fall_df_raw[FALL_LABEL_COL] = 0
    num_dummy_falls_to_create = int(n_rows_fall * 0.05)
    fall_event_len = SAMPLING_RATE_HZ // 2
    for _ in range(num_dummy_falls_to_create):
        start_idx = np.random.randint(0, n_rows_fall - fall_event_len)
        fall_df_raw.loc[start_idx : start_idx + fall_event_len, FALL_LABEL_COL] = 1
        for col in FALL_FEATURE_COLS_RAW[:3]: fall_df_raw.loc[start_idx : start_idx + fall_event_len, col] *= np.random.uniform(3, 7)
        for col in FALL_FEATURE_COLS_RAW[3:]: fall_df_raw.loc[start_idx : start_idx + fall_event_len, col] *= np.random.uniform(2, 6)
    print("Loaded DUMMY fall dataset. RAW FallLabel ('{}') distribution:".format(FALL_LABEL_COL)); print(fall_df_raw[FALL_LABEL_COL].value_counts(normalize=True))

# --- Process Fall Data: Feature Extraction ---
fall_df_featured = pd.DataFrame()
if fall_df_raw is not None and FALL_LABEL_COL in fall_df_raw.columns and fall_df_raw[FALL_LABEL_COL].nunique() > 0:
    print(f"\nExtracting windowed features for fall detection (Window: {WINDOW_SAMPLES} samples, Step: {STEP_SAMPLES} samples)...")
    fall_df_featured = extract_windowed_features(fall_df_raw, WINDOW_SAMPLES, STEP_SAMPLES, FALL_FEATURE_COLS_RAW[:3], FALL_FEATURE_COLS_RAW[3:], FALL_LABEL_COL)
    if fall_df_featured.empty or FALL_LABEL_COL not in fall_df_featured.columns:
        print("Warning: No features extracted or label column missing from featured DataFrame. Fall detection part will be skipped.")
        fall_df_featured = pd.DataFrame()
    else:
        print(f"Extracted {fall_df_featured.shape[1]-1} features from {len(fall_df_featured)} windows.")
        print("\nFeatured fall data distribution (after windowing):"); print(fall_df_featured[FALL_LABEL_COL].value_counts(normalize=True)); print(fall_df_featured[FALL_LABEL_COL].value_counts())

# --- Proceed with Model Training if Features are Available and Diverse ---
if not fall_df_featured.empty and FALL_LABEL_COL in fall_df_featured.columns and fall_df_featured[FALL_LABEL_COL].nunique() > 1:
    y_fall_labels_featured = fall_df_featured[FALL_LABEL_COL]
    X_fall_features_df = fall_df_featured.drop(columns=[FALL_LABEL_COL])
    X_fall_features_df = X_fall_features_df.fillna(method='bfill').fillna(method='ffill').fillna(0)
    X_fall_features_values = X_fall_features_df.values

    X_train_f_orig, X_test_f_orig, y_train_f_orig, y_test_f = train_test_split(
        X_fall_features_values, y_fall_labels_featured, test_size=0.30, random_state=42, stratify=y_fall_labels_featured)

    if IMBLEARN_AVAILABLE:
        minority_class_count = pd.Series(y_train_f_orig).value_counts().min()
        k_neighbors_smote = min(5, minority_class_count - 1) if minority_class_count > 1 else 1 # k must be < n_samples in minority class
        if k_neighbors_smote >= 1 and pd.Series(y_train_f_orig).nunique() > 1: # Check if SMOTE is applicable
            print(f"\nApplying SMOTE to training data (k_neighbors={k_neighbors_smote})...")
            smote = SMOTE(random_state=42, k_neighbors=k_neighbors_smote)
            try:
                X_train_f_resampled, y_train_f_resampled = smote.fit_resample(X_train_f_orig, y_train_f_orig)
                X_train_f_formodel, y_train_f = X_train_f_resampled, y_train_f_resampled
            except ValueError as e_smote:
                print(f"SMOTE Error: {e_smote}. Using original training data.")
                X_train_f_formodel, y_train_f = X_train_f_orig, y_train_f_orig
        else: X_train_f_formodel, y_train_f = X_train_f_orig, y_train_f_orig; print("\nSkipping SMOTE (minority too small or single class).")
    else: X_train_f_formodel, y_train_f = X_train_f_orig, y_train_f_orig; print("\nSMOTE not available.")
    print("Training distribution before/after SMOTE (if applied):"); print("Original:", pd.Series(y_train_f_orig).value_counts().to_dict()); print("For Model:", pd.Series(y_train_f).value_counts().to_dict())


    scaler_fall = StandardScaler()
    X_train_f = scaler_fall.fit_transform(X_train_f_formodel)
    X_test_f = scaler_fall.transform(X_test_f_orig)
    joblib.dump(scaler_fall, 'fall_detection_featured_scaler.pkl')

    print("\nTraining Fall Detection model (RandomForest with GridSearchCV)...")
    param_grid_rf = {
        'n_estimators': [100, 200, 300], 'max_depth': [10, 20, None],
        'min_samples_split': [2, 5, 10], 'min_samples_leaf': [1, 2, 4],
        'class_weight': ['balanced', {0:1, 1:10}, {0:1, 1:15}, {0:1, 1:20}] # More emphasis on fall class
    }
    # Define scorers - prioritize recall for fall class (label 1)
    recall_fall_scorer = make_scorer(recall_score, pos_label=1, average='binary')
    f1_fall_scorer = make_scorer(f1_score, pos_label=1, average='binary')

    if pd.Series(y_train_f).nunique() > 1: # Ensure CV can run
        grid_search_rf = GridSearchCV(RandomForestClassifier(random_state=42, n_jobs=-1),
                                      param_grid_rf, cv=3, scoring=f1_fall_scorer, verbose=1) # Score on F1 for fall
        grid_search_rf.fit(X_train_f, y_train_f)
        print("Best parameters for Fall RF:", grid_search_rf.best_params_)
        fall_model = grid_search_rf.best_estimator_
    else:
        print("Only one class in y_train_f for model. Training basic RF.")
        fall_model = RandomForestClassifier(n_estimators=150, random_state=42, class_weight='balanced', n_jobs=-1)
        fall_model.fit(X_train_f, y_train_f) if X_train_f.shape[0] > 0 else print("No training data for basic RF.")


    if X_train_f.shape[0] > 0: # Only save if model was trained
        joblib.dump(fall_model, 'fall_detection_model.pkl')
        print("Trained Fall Detection model saved.")

        print("\nEvaluating Fall Detection model...")
        y_pred_f = fall_model.predict(X_test_f)
        y_pred_proba_f = fall_model.predict_proba(X_test_f)[:, 1]

        print("\nClassification Report (Fall Detection - Tuned RF):")
        print(classification_report(y_test_f, y_pred_f, target_names=['No Fall (0)', 'Fall (1)'], digits=4, zero_division=0))
        cm_f = confusion_matrix(y_test_f, y_pred_f); sns.heatmap(cm_f, annot=True, fmt='d', cmap='Greens', xticklabels=['No Fall', 'Fall'], yticklabels=['No Fall', 'Fall']); plt.title('Confusion Matrix - Fall Detection'); plt.xlabel('Predicted'); plt.ylabel('True'); plt.show()

        fig_metrics, (ax_roc_f, ax_pr_f) = plt.subplots(1, 2, figsize=(16,6)) # Different ax names
        plot_roc_curve_custom(y_test_f, y_pred_proba_f, "Fall Detection RF", ax=ax_roc_f)
        plot_precision_recall_curve_custom(y_test_f, y_pred_proba_f, "Fall Detection RF", ax=ax_pr_f)
        plt.tight_layout(); plt.show()

        if hasattr(fall_model, 'feature_importances_') and not X_fall_features_df.empty and X_fall_features_df.shape[1] > 0 :
            importances = fall_model.feature_importances_
            feature_names = X_fall_features_df.columns
            if len(importances) == len(feature_names):
                sorted_indices = np.argsort(importances)[::-1]
                plt.figure(figsize=(12, max(6, min(20, len(feature_names)//2)))); plt.title("Top Feature Importances - Fall Detection")
                plt.bar(range(min(20,X_train_f.shape[1])), importances[sorted_indices][:min(20,X_train_f.shape[1])], align="center")
                plt.xticks(range(min(20,X_train_f.shape[1])), feature_names[sorted_indices][:min(20,X_train_f.shape[1])], rotation=90)
                plt.tight_layout(); plt.show()
    else:
        print("Skipping evaluation for fall model as it was not trained.")


elif not fall_df_featured.empty and FALL_LABEL_COL in fall_df_featured.columns and fall_df_featured[FALL_LABEL_COL].nunique() <= 1:
    print("\n--- Skipping Fall Detection Model Training ---")
    print(f"Reason: Featured fall dataset has only one class ('{fall_df_featured[FALL_LABEL_COL].unique()[0]}') after windowing. Model training would be ineffective.")
else:
    print("\n--- Skipping Advanced Fall Detection Model Training ---")
    print("Reason: Critical issues with loading, processing, or diversity of the fall dataset (e.g., no features extracted).")

print("\n\n--- Full Analysis Script Finished ---")