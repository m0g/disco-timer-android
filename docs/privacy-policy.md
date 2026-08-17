# Privacy Policy for Disco Timer

**Last updated: 17 August 2026**

Disco Timer ("the app") is developed by Loïc Nogues. This policy explains what
the app does with your information.

## The short version

**Disco Timer does not collect, store, or share any personal data.**

There are no accounts, no analytics, no advertising, no crash reporting, and no
third-party SDKs. The app does not request the `INTERNET` permission, which
means it is technically incapable of sending any information off your device.

## Information the app collects

None.

The app does not collect your name, email address, phone number, location,
contacts, photos, health or fitness records, device identifiers, advertising
identifiers, or usage analytics.

## Information stored on your device

To save you from re-entering your workout every time, the app stores your timer
settings locally:

- Work interval length
- Number of cycles
- Number of sets
- Preparation countdown length
- Whether sound is muted

These values are plain numbers and an on/off switch. They contain nothing
personal, they never leave your device, and they are held in the app's private
storage, which other apps cannot read. Uninstalling the app deletes them.

If you have Android's built-in Backup feature enabled on your device, Android
may include these settings in your device backup to your own Google account.
That backup is handled entirely by Android and Google, not by Disco Timer, and
the developer has no access to it. You can control or disable this in your
device settings under **Settings → Google → Backup**.

## Permissions the app requests, and why

| Permission | Why it is needed |
| --- | --- |
| `WAKE_LOCK` | Keeps the screen on while a timer is running so you can see it during a workout. |
| `VIBRATE` | Provides haptic feedback on countdown beeps and when the timer finishes. |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps the timer counting accurately when the app is in the background or the screen is off. |
| `POST_NOTIFICATIONS` | Shows the ongoing timer notification while a workout is running. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Lets you optionally exempt the app from battery optimisation so Android does not pause a running timer. |

None of these permissions are used to gather information about you.

## Third parties

Disco Timer includes no third-party analytics, advertising, or tracking
libraries, and shares no data with anyone.

Note that if you installed the app from Google Play, Google collects its own
information about app installs and usage of the Play Store itself, independently
of this app. That is covered by
[Google's Privacy Policy](https://policies.google.com/privacy).

## Children's privacy

The app is suitable for users of all ages. Because it collects no data at all,
it collects no data from children.

## Your rights

Since no personal data is collected or transmitted, there is no data to access,
correct, export, or delete on the developer's side. To remove the settings
stored on your device, either clear the app's storage
(**Settings → Apps → Disco Timer → Storage → Clear data**) or uninstall the app.

## Changes to this policy

If this policy changes, the updated version will be published at this address
and the "Last updated" date above will change. Material changes will also be
noted in the app's release notes.

## Contact

Questions about this policy can be sent to:

**CONTACT_EMAIL_PLACEHOLDER**

Source code for the app is available at
<https://github.com/m0g/disco-timer-android>.
