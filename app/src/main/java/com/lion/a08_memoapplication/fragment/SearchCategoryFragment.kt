package com.lion.a08_memoapplication.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.lion.a08_memoapplication.MainActivity
import com.lion.a08_memoapplication.R
import com.lion.a08_memoapplication.databinding.FragmentSearchCategoryBinding
import com.lion.a08_memoapplication.databinding.RowCategoryManagementBinding
import com.lion.a08_memoapplication.model.CategoryModel
import com.lion.a08_memoapplication.repository.CategoryRepository
import com.lion.a08_memoapplication.repository.MemoRepository
import com.lion.a08_memoapplication.util.FragmentName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SearchCategoryFragment : Fragment() {

    lateinit var fragmentSearchCategoryBinding: FragmentSearchCategoryBinding
    lateinit var mainActivity: MainActivity

    // 리사클리어뷰 구성을 위한 리스트
    var categoryList = mutableListOf<CategoryModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        fragmentSearchCategoryBinding = FragmentSearchCategoryBinding.inflate(inflater)
        mainActivity= activity as MainActivity

        // 툴바 구현 메서드
        settingToolbarSearchCategory()
        // recyclerView 구성 메서드
        settingRecyclerViewSearchCategory()
        // 입력 요소 설정 메서드
        settingTextField()
        
        return fragmentSearchCategoryBinding.root
    }

    // 툴바 구현 메서드
    fun settingToolbarSearchCategory(){
        fragmentSearchCategoryBinding.apply {
            toolbarSearchCategory.title = "카테고리 정보 검색"

            toolbarSearchCategory.setNavigationIcon(R.drawable.arrow_back_24px)
            toolbarSearchCategory.setNavigationOnClickListener {
                mainActivity.removeFragment(FragmentName.SEARCH_CATEGORY_FRAGMENT)
            }
        }
    }

    // recyclerView 구성 메서드
    fun settingRecyclerViewSearchCategory(){
        fragmentSearchCategoryBinding.apply {
            recyclerViewSearchCategory.adapter = RecyclerViewCategorySearchAdapter()
            recyclerViewSearchCategory.layoutManager = LinearLayoutManager(mainActivity)
            val deco = MaterialDividerItemDecoration(mainActivity, MaterialDividerItemDecoration.VERTICAL)
            recyclerViewSearchCategory.addItemDecoration(deco)
        }
    }

    // Recyclerview의 어뎁터
    inner class RecyclerViewCategorySearchAdapter : RecyclerView.Adapter<RecyclerViewCategorySearchAdapter.ViewHolderCategorySearchAdapter>(){

        // ViewHolder
        inner class ViewHolderCategorySearchAdapter(val rowCategoryManagementBinding: RowCategoryManagementBinding) : RecyclerView.ViewHolder(rowCategoryManagementBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ViewHolderCategorySearchAdapter {

            val rowCategoryManagementBinding = RowCategoryManagementBinding.inflate(layoutInflater,parent,false)
            val viewHolderCategorySearchAdapter = ViewHolderCategorySearchAdapter(rowCategoryManagementBinding)

            // 버튼을 누르면 동작하는 리스너
            rowCategoryManagementBinding.buttonRowCategoryManagement.setOnClickListener {
                // BottomSheet를 띄운다.
                val searchCategoryBottomSheetFragment = SearchCategoryBottomSheetFragment(
                    this@SearchCategoryFragment,
                    viewHolderCategorySearchAdapter.adapterPosition,
                    categoryList[viewHolderCategorySearchAdapter.adapterPosition].categoryIdx,
                    categoryList[0].categoryIdx
                )
                searchCategoryBottomSheetFragment.show(mainActivity.supportFragmentManager,"BottomSheet")
            }

            return viewHolderCategorySearchAdapter
        }

        override fun getItemCount(): Int {
            return categoryList.size
        }

        override fun onBindViewHolder(holder: ViewHolderCategorySearchAdapter, position: Int) {
            holder.rowCategoryManagementBinding.textViewRowCategoryManagement.text = categoryList[position].categoryName
        }

    }

    // 입력 요소 설정
    fun settingTextField(){
        fragmentSearchCategoryBinding.apply {
            mainActivity.showSoftInput(textFieldSearchCategoryName.editText!!)
            // 키보드의 엔터를 누르면 동작하는 리스너
            textFieldSearchCategoryName.editText?.setOnEditorActionListener { v, actionId, event ->
                // 검색 데이터를 가져와 보여준다.
                CoroutineScope(Dispatchers.Main).launch {
                    val work1 = async(Dispatchers.IO){
                        val keyword = textFieldSearchCategoryName.editText?.text.toString()
                       // MemoRepository.selectMemoDataAllByMemoTitle(mainActivity, keyword)
                        CategoryRepository.selectCategoryAllByCategoryName(mainActivity,keyword)
                    }
                    categoryList = work1.await()
                    recyclerViewSearchCategory.adapter?.notifyDataSetChanged()
                }
                mainActivity.hideSoftInput()
                true
            }
        }
    }



}