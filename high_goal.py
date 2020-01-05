#!/usr/bin/env python3

import numpy as np
import cv2
import math

import sys

# Location of a target in an image
class TargetLocation:
  # x, y, width, height locations are in range of 0.0 - 1.0, pitch and yaw are in radians
  def __init__(self, x, y, width, height, yaw, pitch):
    self.x = x
    self.y = y
    self.width = width
    self.height = height

    self.yaw = yaw
    self.pitch = pitch
  
  # draw on an image
  def drawOnImage(self, image):
    img_h, img_w, _ = image.shape
    cv2.rectangle(image, (int(self.x * img_w), int(self.y * img_h)), (int((self.x + self.width) * img_w), int((self.y + self.height) * img_h)), (0, 0, 255), 1)


# Given an image, detect targets in it
# Returns array of targets found, in order of largest to smallest

# image - image to perform processing on
# resize_width, resize_height - size to resize image to for processing (keep small)
# low_hsv, high_hsv - range of acceptable hsv values for targets (green leds)
# blur_radius - radius of blur to apply to remove noise (must be odd int)
# min_area_ratio - minimum amount of area of the image (0.0 - 1.0) the target's convex hull must occupy to be considered valid
# diag_field_view - diagonal field of view of camera in radians,
# aspect_h, aspect_v - aspect ratio
def high_goal_detect(
    image, 
    resize_width=320, 
    resize_height=240,
    low_hsv=np.array([0,220,25]), 
    high_hsv=np.array([101, 255, 255]),
    blur_radius=5,
    min_area_ratio=0.001,
    diag_field_view = math.radians(68.5), #Lifecam 3000
    aspect_h=16,  #Lifecam 3000
    aspect_v=9,   #Lifecam 3000
  ):

  # calculate camera information
  aspect_diag = math.hypot(aspect_h, aspect_v)

  field_view_h = math.atan(math.tan(diag_field_view/2) * (aspect_h / aspect_diag)) * 2
  field_view_v = math.atan(math.tan(diag_field_view/2) * (aspect_v / aspect_diag)) * 2

  h_focal_len = resize_width / (2*math.tan((field_view_h/2)))
  v_focal_len = resize_height / (2*math.tan((field_view_v/2)))

  #calculate yaw or pitch (in radians)
  def calc_angle(x, center_x, focal_len):
    return math.atan((x - center_x) / focal_len)

  # resize
  image_rs = cv2.resize(image, (resize_width, resize_height))
  # blur
  image_blur = cv2.blur(image_rs, (blur_radius, blur_radius))
  # convert to hsv
  image_hsv = cv2.cvtColor(image_blur, cv2.COLOR_BGR2HSV)
  # mask hsv to specified range
  image_mask = cv2.inRange(image_hsv, low_hsv, high_hsv)
  # find contours
  contours, _ = cv2.findContours(image_mask, cv2.RETR_TREE, cv2.CHAIN_APPROX_TC89_KCOS)
  # sort contours (large -> small)
  contours = sorted(contours, key=lambda x: cv2.contourArea(x), reverse=True)

  targets = []

  for cnt in contours:
    # convex hull area
    hull_area = cv2.contourArea(cv2.convexHull(cnt))
    image_area = resize_width * resize_height
    if float(hull_area) / float(image_area) >= min_area_ratio:
      # find bounding box for countour
      x, y, w, h = cv2.boundingRect(cnt)
      # because vision target is just on the lower half of the real target, adjust box to include top half
      y -= h
      h *= 2

      # find center of bounding rectangle
      cx = x + int(w/2)
      cy = y + int(h/2)

      # calculate yaw + pitch offset (in radians)
      yaw = calc_angle(cx, resize_width/2, h_focal_len)
      pitch = calc_angle(cy, resize_height/2, v_focal_len)

      targets.append(TargetLocation(x/resize_width, y/resize_height, w/resize_width, h/resize_height, yaw, pitch))

  return targets

if len(sys.argv) < 2:
  print("Usage: high_goal.py image1 image2 ...")
  exit(1)

for path in sys.argv[1:]:
  img = cv2.imread(path)
  targets = high_goal_detect(img)
  if(len(targets) >= 1):
    targets[0].drawOnImage(img)
    print(math.degrees(targets[0].yaw))
    cv2.imshow("targets", img)
    while(cv2.waitKey(0) & 0xff != ord('q')):
      pass
    cv2.destroyAllWindows()
