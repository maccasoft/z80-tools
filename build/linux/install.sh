#!/bin/sh

# This script adds a menu item, icons and mime type for SWT Application for the
# current user. If possible, it will use the xdg-utils - or fall back to just
# creating and copying a desktop file to the user's dir.
# If called with the "-u" option, it will undo the changes.

# Resource name to use (including vendor prefix)
RESOURCE_NAME=maccasoft-z80tools
SCRIPT_NAME=launcher

# Get absolute path from which this script file was executed
# (Could be changed to "pwd -P" to resolve symlinks to their target)
SCRIPT_PATH=$( cd $(dirname $0) ; pwd )
cd "${SCRIPT_PATH}"

# Default mode is to install.
UNINSTALL=false

# If possible, get location of the desktop folder. Default to ~/Desktop
XDG_DESKTOP_DIR="${HOME}/Desktop"
if [ -f "${XDG_CONFIG_HOME:-${HOME}/.config}/user-dirs.dirs" ]; then
  . "${XDG_CONFIG_HOME:-${HOME}/.config}/user-dirs.dirs"
fi

# Install using xdg-utils
xdg_install_f() {

  # Create a temp dir accessible by all users
  TMP_DIR=`mktemp --directory`

  # Create *.desktop file using the existing template file
  sed -e "s,<BINARY_LOCATION>,${SCRIPT_PATH}/${SCRIPT_NAME},g" \
      -e "s,<ICON_NAME>,${RESOURCE_NAME},g" "${SCRIPT_PATH}/lib/desktop.template" > "${TMP_DIR}/${RESOURCE_NAME}.desktop"

  # Install the icon files using name and resolutions
  xdg-icon-resource install --context apps --size 16 "${SCRIPT_PATH}/lib/icons/app16.png" $RESOURCE_NAME
  xdg-icon-resource install --context apps --size 32 "${SCRIPT_PATH}/lib/icons/app32.png" $RESOURCE_NAME
  xdg-icon-resource install --context apps --size 48 "${SCRIPT_PATH}/lib/icons/app48.png" $RESOURCE_NAME
  xdg-icon-resource install --context apps --size 64 "${SCRIPT_PATH}/lib/icons/app64.png" $RESOURCE_NAME

  # Install the created *.desktop file
  xdg-desktop-menu install "${TMP_DIR}/${RESOURCE_NAME}.desktop"

  # Clean up
  rm "${TMP_DIR}/${RESOURCE_NAME}.desktop"
  rmdir "$TMP_DIR"

}

# Install by simply copying desktop file (fallback)
simple_install_f() {

  # Create a temp dir accessible by all users
  TMP_DIR=`mktemp --directory`

  # Create *.desktop file using the existing template file
  sed -e "s,<BINARY_LOCATION>,${SCRIPT_PATH}/${SCRIPT_NAME},g" \
      -e "s,<ICON_NAME>,${SCRIPT_PATH}/${SCRIPT_NAME}.png,g" "${SCRIPT_PATH}/lib/desktop.template" > "${TMP_DIR}/${RESOURCE_NAME}.desktop"

  mkdir -p "${HOME}/.local/share/applications"
  cp "${TMP_DIR}/${RESOURCE_NAME}.desktop" "${HOME}/.local/share/applications/"

  # Clean up temp dir
  rm "${TMP_DIR}/${RESOURCE_NAME}.desktop"
  rmdir "${TMP_DIR}"

}

# Uninstall using xdg-utils
xdg_uninstall_f() {

  # Remove *.desktop file
  xdg-desktop-menu uninstall ${RESOURCE_NAME}.desktop

  # Remove icon from desktop
  xdg-desktop-icon uninstall ${RESOURCE_NAME}.desktop

  # Remove icons
  xdg-icon-resource uninstall --size 16 ${RESOURCE_NAME}
  xdg-icon-resource uninstall --size 32 ${RESOURCE_NAME}
  xdg-icon-resource uninstall --size 48 ${RESOURCE_NAME}
  xdg-icon-resource uninstall --size 64 ${RESOURCE_NAME}

}

# Uninstall by simply removing desktop files (fallback), incl. old one
simple_uninstall_f() {

  if [ -f "${HOME}/.local/share/applications/${RESOURCE_NAME}.desktop" ]; then
    rm "${HOME}/.local/share/applications/${RESOURCE_NAME}.desktop"
  fi

  if [ -f "${XDG_DESKTOP_DIR}/${RESOURCE_NAME}.desktop" ]; then
    rm "${XDG_DESKTOP_DIR}/${RESOURCE_NAME}.desktop"
  fi

}

# Update desktop file and mime databases (if possible)
updatedbs_f() {

  if [ -d "${HOME}/.local/share/applications" ]; then
    if command -v update-desktop-database > /dev/null; then
      update-desktop-database "${HOME}/.local/share/applications"
    fi
  fi

}

# Check availability of xdg-utils
xdg_exists_f() {

  if ! command -v xdg-icon-resource > /dev/null; then return 1; fi
  if ! command -v xdg-desktop-menu > /dev/null; then return 1; fi
  if ! command -v xdg-desktop-icon > /dev/null; then return 1; fi
  return 0

}

# Shows a description of the available options
display_help_f() {
  printf "\nThis script will add a SWT Application desktop shortcut, menu item,\n"
  printf "icons and file associations for the current user.\n"
  if ! xdg_exists_f; then
    printf "\nxdg-utils are recommended to be installed, so this script can use them.\n"
  fi
  printf "\nOptional arguments are:\n\n"
  printf "\t-u, --uninstall\t\tRemoves shortcut, menu item and icons.\n\n"
  printf "\t-h, --help\t\tShows this help again.\n\n"
}

# Check for provided arguments
while [ $# -gt 0 ] ; do
  ARG="${1}"
  case $ARG in
      -u|--uninstall)
        UNINSTALL=true
        shift
      ;;
      -h|--help)
        display_help_f
        exit 0
      ;;
      *)
        printf "\nInvalid option -- '${ARG}'\n"
        display_help_f
        exit 1
      ;;
  esac
done

# If possible, use xdg-utils, if not, use a more basic approach
if xdg_exists_f; then
  if [ ${UNINSTALL} = true ]; then
    printf "Removing desktop shortcut and menu item for SWT Application..."
    xdg_uninstall_f
    simple_uninstall_f
  else
    printf "Adding desktop shortcut, menu item and file associations for SWT Application..."
    xdg_uninstall_f
    simple_uninstall_f
    xdg_install_f
  fi
else
  if [ ${UNINSTALL} = true ]; then
    printf "Removing desktop shortcut and menu item for SWT Application..."
    simple_uninstall_f
  else
    printf "Adding desktop shortcut and menu item for SWT Application..."
    simple_uninstall_f
    simple_install_f
  fi
fi
updatedbs_f
printf " done!\n"

exit 0
