/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.nimingban.component.paper

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hippo.drawerlayout.DrawerLayout
import com.hippo.nimingban.R
import com.hippo.nimingban.component.GroupPaper
import com.hippo.nimingban.util.dimensionPixelSize
import com.hippo.nimingban.util.find
import com.hippo.nimingban.widget.nmb.NmbDrawerSide

/*
 * Created by Hippo on 2017/7/15.
 */

class DrawerPaper(
    private val logic: DrawerLogic
) : GroupPaper<DrawerUi>(logic), DrawerUi {

  companion object {
    const val CONTAINER_ID_CONTENT = R.id.drawer_content
    const val CONTAINER_ID_LEFT = R.id.left_drawer
    const val CONTAINER_ID_RIGHT = R.id.right_drawer
  }

  private lateinit var drawerLayout: DrawerLayout
  private lateinit var drawerContent: ViewGroup
  private lateinit var leftDrawer: NmbDrawerSide
  private lateinit var rightDrawer: NmbDrawerSide

  override fun onCreate(inflater: LayoutInflater, container: ViewGroup) {
    super.onCreate(inflater, container)

    view = inflater.inflate(R.layout.paper_drawer, container, false)
    drawerLayout = view.find(R.id.drawer_layout)
    drawerContent = view.find(R.id.drawer_content)
    leftDrawer = view.find(R.id.left_drawer)
    rightDrawer = view.find(R.id.right_drawer)

    drawerLayout.setDrawerListener(object : DrawerLayout.DrawerListener {
      override fun onDrawerStateChanged(view: View?, state: Int) {}
      override fun onDrawerSlide(view: View?, percent: Float) {}
      override fun onDrawerClosed(view: View?) {
        if (view == rightDrawer) {
          logic.onCloseRightDrawer()
        }
      }
      override fun onDrawerOpened(view: View?) {
        if (view == rightDrawer) {
          logic.onOpenRightDrawer()
        }
      }
    })
  }

  override fun setLeftDrawerWidth(resId: Int) {
    leftDrawer.layoutParams.width = context.dimensionPixelSize(resId)
    leftDrawer.requestLayout()
  }

  override fun setRightDrawerWidth(resId: Int) {
    rightDrawer.layoutParams.width = context.dimensionPixelSize(resId)
    rightDrawer.requestLayout()
  }

  override fun setLeftDrawerMode(mode: Int) {
    leftDrawer.mode = mode
  }

  override fun setRightDrawerMode(mode: Int) {
    rightDrawer.mode = mode
  }

  override fun closeDrawers() {
    drawerLayout.closeDrawers()
  }

  override fun openLeftDrawer() {
    drawerLayout.openDrawer(Gravity.LEFT)
  }

  override fun openRightDrawer() {
    drawerLayout.openDrawer(Gravity.RIGHT)
  }
}
